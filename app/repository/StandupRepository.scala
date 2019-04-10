package repository

import models.{Standup, StandupContext, TeamUpdate}

import scala.concurrent.Future

trait StandupRepository {

  type StandupName = String

  def get(standupName: String): Option[Standup]

  def add(standup: Standup): Future[Standup]

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
      teamUpdate  <- context.startNext()
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