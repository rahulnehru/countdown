package repository.postgres

import java.time.Duration

import cats.data.NonEmptyList
import models.{Standup, Team}
import repository.postgres.Schema.standUps
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

object Schema {

  case class StandUpTable(id: Long = 0L, name: String, displayName: String)

  class StandUps(tag: Tag) extends Table[StandUpTable](tag, "stand_ups") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def displayName = column[String]("display_name")

    override def * = (id, name, displayName) <> (StandUpTable.tupled, StandUpTable.unapply)
  }

  val standUps = TableQuery[StandUps]

  case class TeamTable(id: Long = 0L, name: String, speaker: String, allocationInSeconds: Long, standUpId: Long)

  class Teams(tag: Tag) extends Table[TeamTable](tag, "teams") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def speaker = column[String]("speaker")

    def allocationInSeconds = column[Long]("allocation_in_seconds")

    def standUpId = column[Long]("stand_up_id")

    def standUp = foreignKey("stand_up_FK", standUpId, standUps)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    override def * = (id, name, speaker, allocationInSeconds, standUpId) <> (TeamTable.tupled, TeamTable.unapply)
  }

  val teams = TableQuery[Teams]

  object SlickQuery {

    val allStandUps = for {
      team <- teams
      standUp <- standUps
      if standUp.id === team.standUpId
    } yield (standUp, team)

    val findStandUpByName: String => Query[(StandUps, Teams), (StandUpTable, TeamTable), Seq] = (name: String) =>
      for {
        standUp <- standUps.filter(_.name === name)
        team <- teams
        if standUp.id === team.standUpId
      } yield (standUp, team)


    def removeTeamsFromStandUp(teamNames: Seq[String]) = teams.filter(_.name.inSet(teamNames)).delete

  }

  object SlickAction {

    def addNewTeams(standUpName: String, newTeams: Seq[Team])(implicit ec: ExecutionContext) =
      for {
        mayBeId <- standUps.filter(_.name === standUpName).map(_.id).result.headOption
        newTeams <- DBIO.successful(mayBeId.toSeq.flatMap(standUpId => newTeams.map(t => TeamTable(name = t.name, speaker = t.speaker, allocationInSeconds = t.allocationInSeconds.getSeconds, standUpId = standUpId))))
        rowsInserted <- teams ++= newTeams
      } yield rowsInserted

    def deleteStandUp(standUp: Standup)(implicit ec: ExecutionContext) = for {
      standUpExists <- standUps.filter(_.name === standUp.name).exists.result
      if(standUpExists)
      standUpMatched <- standUps.filter(_.name === standUp.name).result.head
      _ <- teams.filter(_.standUpId === standUpMatched.id).delete
      standUpDeleted <- standUps.filter(_.name === standUp.name).delete
    } yield standUpDeleted > 0

    def addStandUp(standUp: Standup)(implicit ec: ExecutionContext) = for {
      standUpId <- standUps returning standUps.map(_.id) += StandUpTable(name = standUp.name, displayName = standUp.displayName)
      teamIds <- teams returning teams.map(_.id) ++= Iterable(standUp.teams.map(t => TeamTable(name = t.name, speaker = t.speaker, allocationInSeconds = t.allocationInSeconds.getSeconds, standUpId = standUpId)).toList: _*)
      standUpAdded <- SlickQuery.findStandUpByName(standUp.name).result
    } yield buildStandUp(standUpAdded)

    def buildStandUp(results: Seq[(StandUpTable, TeamTable)]): List[Standup] =
    results.groupBy(_._1)
      .mapValues(_.map(_._2)).map {
      case (standUp, teams) => Standup(standUp.id, standUp.name, standUp.displayName, NonEmptyList.fromListUnsafe(teams.map(t => Team(t.id, t.name, t.speaker, Duration.ofSeconds(t.allocationInSeconds))).toList))
    }.toList

  }

}
