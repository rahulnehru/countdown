package repository

import com.google.inject.ImplementedBy
import models.{Standup, StandupContext, Team, TeamUpdate}
import repository.postgres.PostgresStandUpRepository

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PostgresStandUpRepository])
trait StandUpRepository {

  implicit val ec: ExecutionContext

  type StandUpName = String

  def exists(standUpName: StandUpName): Future[Boolean] = find(standUpName).map(_.isDefined)

  def find(standUpName: StandUpName): Future[Option[Standup]]

  def add(standUp: Standup): Future[Standup]

  def addTeams(standUpName: StandUpName, team: Set[Team]): Future[Int]

  def removeTeams(teamNames: Set[String]): Future[Int]

  def edit(standUp: Standup): Future[Standup]

  def delete(standUp: Standup): Future[Boolean]

  def getAll: Future[Set[Standup]]

  var currentStandupsContext: Map[StandUpName, StandupContext] = Map.empty

  private def addToContext(context: StandupContext): StandupContext = {
    currentStandupsContext += (context.standupName -> context)
    context
  }

  def start(name: StandUpName)(implicit ec: ExecutionContext): Future[Option[TeamUpdate]] = {
    getAll.map { standUps =>
      val context = StandupContext(name, standUps)
      for {
        teamUpdate  <- context.startStandup()
        _           = addToContext(context)
      } yield teamUpdate
    }
  }

  def pause(name: StandUpName): Option[TeamUpdate] =
    for {
      context     <- currentStandupsContext.get(name)
      teamUpdate  <- context.pause()
      _           = addToContext(context)
    } yield teamUpdate

  def unpause(name: StandUpName): Option[TeamUpdate] =
    for {
      context     <- currentStandupsContext.get(name)
      teamUpdate  <- context.unpause()
      _           = addToContext(context)
    } yield teamUpdate

  def next(name: StandUpName): Option[TeamUpdate] = for {
    context     <- currentStandupsContext.get(name)
    teamUpdate  <- context.startNext()
    _           = addToContext(context)
  } yield teamUpdate

  def hasNext(name: StandUpName): Boolean = currentStandupsContext.get(name).exists(!_.left().isEmpty)

  def status(name: StandUpName): Option[TeamUpdate] = for {
    context     <- currentStandupsContext.get(name)
    teamUpdate  <- context.inProgress()
  } yield teamUpdate

  def stop(name: StandUpName): Boolean = {
    currentStandupsContext = currentStandupsContext
      .filter { case (key, _) => name != key }

    currentStandupsContext.exists { case (key, _) => name == key }
  }

}