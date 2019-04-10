package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.Message
import play.api.libs.json.Json.toJson
import repository.StandupRepository

import scala.concurrent.duration._

class StandupClientCountdownServiceActor(out: ActorRef, standupName: String, standupRepository: StandupRepository) extends Actor {

  private def isValidStandup: Boolean = standupRepository.getAll.exists(_.name == standupName)
  private def isInProgress: Boolean = standupRepository.status(standupName).exists(_.countdown.remaining() >= 0)

  val cancellable =
    context.system.scheduler.schedule(
      0 milliseconds,
      1000 milliseconds,
      self,
      "status")(context.system.dispatcher)

  override def receive: Receive = {
    case "disconnect" =>
      println("No live standup")
      out ! toJson(Message(s"Disconnecting from $standupName."))
      cancellable.cancel()
      self ! PoisonPill
    case "status" =>
      if(isValidStandup && isInProgress)
        out ! toJson(standupRepository.status(standupName))
      else
        self ! "disconnect"
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing client connection")
  }
}

object StandupClientCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandupRepository) = Props(new StandupClientCountdownServiceActor(out, standupName, standupRepo: StandupRepository))
}
