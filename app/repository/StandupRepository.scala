package repository

import com.google.inject.ImplementedBy
import models.{Standup, StandupContext, Team, TeamUpdate}
import repository.postgres.PostgresStandupRepository

import scala.concurrent.Future

@ImplementedBy(classOf[PostgresStandupRepository])
trait StandupRepository {

  type StandupName = String

  def find(standupName: String): Option[Standup]

  def add(standup: Standup): Future[Standup]

  def addTeams(standUpName: String, team: Set[Team]): Future[Int]

  def removeTeams(teamNames: Set[String]): Future[Int]

  def edit(standup: Standup): Future[Standup]

  def delete(standup: Standup): Future[Boolean]

  def getAll: List[Standup]

  var currentStandupsContext: Map[StandupName, StandupContext] = Map.empty

  private def addToContext(context: StandupContext): StandupContext = {
    currentStandupsContext += (context.standupName -> context)
    context
  }

  def start(name: StandupName): Option[TeamUpdate] = {
    val context = StandupContext(name, getAll)
    for {
      teamUpdate  <- context.startStandup()
      _           = addToContext(context)
    } yield teamUpdate

  }

  def pause(name: StandupName): Option[TeamUpdate] =
    for {
      context     <- currentStandupsContext.get(name)
      teamUpdate  <- context.pause()
      _           = addToContext(context)
    } yield teamUpdate

  def unpause(name: StandupName): Option[TeamUpdate] =
    for {
      context     <- currentStandupsContext.get(name)
      teamUpdate  <- context.unpause()
      _           = addToContext(context)
    } yield teamUpdate

  def next(name: StandupName): Option[TeamUpdate] = for {
    context     <- currentStandupsContext.get(name)
    teamUpdate  <- context.startNext()
    _           = addToContext(context)
  } yield teamUpdate

  def hasNext(name: StandupName): Boolean = currentStandupsContext.get(name).exists(!_.left().isEmpty)

  def status(name: StandupName): Option[TeamUpdate] = for {
    context     <- currentStandupsContext.get(name)
    teamUpdate  <- context.inProgress()
  } yield teamUpdate

  def stop(name: StandupName): Boolean = {
    currentStandupsContext = currentStandupsContext
      .filter { case (key, _) => name != key }

    currentStandupsContext.exists { case (key, _) => name == key }
  }

}