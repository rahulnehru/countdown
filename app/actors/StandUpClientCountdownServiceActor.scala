package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.{Message, TeamUpdate}
import play.api.libs.json.Json.toJson
import repository.StandUpRepository
import akka.pattern.pipe
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class StandUpClientCountdownServiceActor(out: ActorRef, standUpName: String, standupRepository: StandUpRepository) extends Actor {

  implicit val ec: ExecutionContext  = context.dispatcher

  private def isInProgress: Boolean = standupRepository.status(standUpName).exists(_.countdown.remaining() >= 0)

  val cancellable =
    context.system.scheduler.schedule(
      0 milliseconds,
      1000 milliseconds,
      self,
      "status")(context.system.dispatcher)

  override def receive: Receive = {
    case "disconnect" =>
      println("No live standup")
      out ! toJson(Message(s"Disconnecting from $standUpName."))
      cancellable.cancel()
      self ! PoisonPill
    case "status" =>
      standupRepository.exists(standUpName).map { exists =>
        if(exists && isInProgress)
          standupRepository.status(standUpName)
        else
          "disconnect"
      }.pipeTo(self)
    case t: Option[TeamUpdate] =>
      t.fold(self ! "disconnect")(out ! toJson(_))
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing client connection")
  }
}

object StandUpClientCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandUpRepository) = Props(new StandUpClientCountdownServiceActor(out, standupName, standupRepo: StandUpRepository))
}
