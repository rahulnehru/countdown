package repository.postgres

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

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

  object Query {

    val allStandUps = (for {
      team <- teams
      standUp <- standUps
      if standUp.id === team.standUpId
    } yield (standUp, team))

    val findStandUpByName = (name: String) => for {
      standUp <- standUps.filter(_.name === name)
      team <- teams
      if standUp.id === team.standUpId
    } yield (standUp, team)

  }
}
