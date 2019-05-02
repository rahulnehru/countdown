package repository.postgres


import java.time.Duration

import cats.data.NonEmptyList
import javax.inject.{Inject, Singleton}
import models.{Standup, Team}
import play.api.Logging
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.StandUpRepository
import repository.postgres.Schema.SlickAction._
import repository.postgres.Schema.SlickQuery._
import repository.postgres.Schema._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostgresStandUpRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends StandUpRepository with HasDatabaseConfigProvider[JdbcProfile] with Logging {
  import profile.api._

  val durationToWait = {
    import scala.concurrent.duration._
    10.seconds
  }

  override def find(standUpName: String): Future[Option[Standup]] =
    db.run {
      findStandUpByName(standUpName).result.map(buildStandUp(_).headOption)
    }


  override def add(standup: Standup): Future[Standup] = {
    def addStandUp() = db.run {
      standUps returning standUps.map(_.id) += StandUpTable(name = standup.name, displayName = standup.displayName)
    }

    def addTeam(standUpId: Long) = db.run {

      teams returning teams.map(_.id) ++= Iterable(standup.teams.map(t => TeamTable(name = t.name, speaker = t.speaker, allocationInSeconds = t.allocationInSeconds.getSeconds, standUpId = standUpId)).toList: _*)
    }

    for {
      standUpId <- addStandUp()
      _ <- addTeam(standUpId)
    } yield standup.copy(id = standUpId)
  }

  override def edit(standup: Standup): Future[Standup] = ???

  override def delete(standup: Standup): Future[Boolean] = ???

  override def getAll: Future[Set[Standup]] = {
    db.run {
      allStandUps.result.map { results =>
        results.groupBy(_._1)
          .mapValues(_.map(_._2)).map {
          case (standUp, teams) => Standup(standUp.id, standUp.name, standUp.displayName, NonEmptyList.fromListUnsafe(teams.map(t => Team(t.id, t.name, t.speaker, Duration.ofSeconds(t.allocationInSeconds))).toList))
        }
      }.map(_.toSet)
    }
  }

  override def addTeams(standUpName: String, newTeams: Set[Team]) = db.run {
    addNewTeams(standUpName, newTeams.toSeq)
  }.flatMap(_.fold(Future.failed[Int](new IllegalStateException(s"Unable to insert teams $newTeams in $standUpName standup")))(Future.successful(_)))//TODO use monad transformer

  private def buildStandUp(results: Seq[(StandUpTable, TeamTable)]): List[Standup] =
    results.groupBy(_._1)
    .mapValues(_.map(_._2)).map {
    case (standUp, teams) => Standup(standUp.id, standUp.name, standUp.displayName, NonEmptyList.fromListUnsafe(teams.map(t => Team(t.id, t.name, t.speaker, Duration.ofSeconds(t.allocationInSeconds))).toList))
  }.toList


  override def removeTeams(teamNames: Set[String]): Future[Int] = db.run(removeTeamsFromStandUp(teamNames.toSeq))
}