package repository

import models.countdown.{InMemoryDB, Standup, StandupContext, TeamUpdate}

trait StandupRepository {

  type StandupName = String

  def standups: List[Standup]

  var currentStandupsContext: Map[StandupName, StandupContext] = Map.empty

  private def add(context: StandupContext): StandupContext = {
    currentStandupsContext += (context.standupName -> context)
    context
  }

  def start(name: StandupName): Option[TeamUpdate] = {
    val context = StandupContext(name, standups)
    for {
      teamUpdate  <- context.startNext()
      _           = add(context)
    } yield teamUpdate

  }

  def pause(name: StandupName): Option[TeamUpdate] =
    for {
      context     <- currentStandupsContext.get(name)
      teamUpdate  <- context.pause()
      _           = add(context)
    } yield teamUpdate

  def next(name: StandupName): Option[TeamUpdate] = for {
    context     <- currentStandupsContext.get(name)
    teamUpdate  <- context.startNext()
    _           = add(context)
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


class InMemoryStandupRepository extends StandupRepository {

  var standupsContext: Map[StandupName, StandupContext] = Map.empty

  override def standups: List[Standup] = InMemoryDB.standups

}