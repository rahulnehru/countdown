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
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostgresStandUpRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends StandUpRepository with HasDatabaseConfigProvider[JdbcProfile] with Logging {
  import profile.api._

  val durationToWait = {
    import scala.concurrent.duration._
    10.seconds
  }

  override def find(standUpName: String): Future[Option[Standup]] = db.run {
    findStandUpByName(standUpName).result.map(buildStandUp(_).headOption)
  }

  override def add(standUp: Standup): Future[Standup] = db.run {
    for{
      newStandUp <- addStandUp(standUp)
    } yield newStandUp
  }.safeHead

  override def edit(standUp: Standup): Future[Standup] = db.run {
    for {
      deleted <- deleteStandUp(standUp)
      modifiedStandUp <- addStandUp(standUp)
    } yield modifiedStandUp
  }.safeHead


  override def delete(standUp: Standup): Future[Boolean] = db.run {
    deleteStandUp(standUp)
  }

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
  }.flatMap(_.fold(Future.failed[Int](new IllegalStateException(s"Unable to insert teams $newTeams in $standUpName standUp")))(Future.successful(_)))//TODO use monad transformer

  implicit class RichFutureContainingSeq[T](f: Future[Seq[T]]) {
    def safeHead: Future[T] =
      f.flatMap(_.headOption
          .fold(Future.failed[T](new IllegalStateException(s"Operation failed returning no results")))(
            Future.successful(_)
          )
        )
  }

  override def removeTeams(teamNames: Set[String]): Future[Int] = db.run(removeTeamsFromStandUp(teamNames.toSeq))
}