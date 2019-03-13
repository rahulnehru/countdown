package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.countdown._
import play.api.libs.json.Json.toJson
import repository.StandupRepository
import scala.concurrent.duration._

class StandupClientCountdownServiceActor(out: ActorRef, standupName: String, standupRepository: StandupRepository) extends Actor {

  private def isValidStandup() = standupRepository.standups.exists(_.name == standupName)
  private def isInProgress() = standupRepository.status(standupName).exists(_.countdown.remaining() > 0)

  val cancellable =
    context.system.scheduler.schedule(
      0 milliseconds,
      1000 milliseconds,
      self,
      "status")(context.system.dispatcher)

  override def receive: Receive = {
    case "disconnect" =>
      out ! toJson(Message(s"Disconnecting from $standupName."))
      cancellable.cancel()
      self ! PoisonPill
    case "status" =>
      if(isValidStandup() && isInProgress())
        out ! toJson(standupRepository.status(standupName))
      else
        self ! "disconnect"
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing connection")
  }
}

object StandupClientCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandupRepository) = Props(new StandupClientCountdownServiceActor(out, standupName, standupRepo: StandupRepository))
}
