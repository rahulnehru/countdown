package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.pipe
import models.Message
import play.api.libs.json.Json.toJson
import repository.StandUpRepository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class StandupAdminCountdownServiceActor(out: ActorRef, standUpName: String, standupRepository: StandUpRepository) extends Actor {

  implicit val ec: ExecutionContext  = context.dispatcher

  standupRepository
    .exists(standUpName)
    .fallbackTo(Future.successful(false))
    .map(exist => if(exist) "start" else PoisonPill)
    .pipeTo(self)

  val cancellable =
    context.system.scheduler.schedule(
      0 milliseconds,
      1000 milliseconds,
      self,
      "status")(context.system.dispatcher)

  override def receive: Receive = {
    case "connect" =>
      println(s"${this.getClass.getCanonicalName} - Connected an admin")
      context.become(connected)
  }

  def connected: Receive = {
    case "start" =>
      println(s"${this.getClass.getCanonicalName} - Started standup")
      standupRepository.start(standUpName)
      context.become(connected)
    case "join" =>
      println(s"${this.getClass.getCanonicalName} - Joined standup")
      println(standUpName)
      context.become(connected)
    case "status" =>
      out ! toJson(standupRepository.status(standUpName))
    case "next" =>
      println("Next person")
      standupRepository.next(standUpName).fold(self ! "stop")(_ => self ! "status")
    case "unpause" =>
      println("Unpause / continue")
      standupRepository.unpause(standUpName)
      self ! "status"
    case "pause" =>
      println("Pause")
      out ! toJson(Message(s"Speaker $standUpName paused"))
      standupRepository.pause(standUpName)
      self ! "status"
    case "stop" =>
      out ! toJson(Message(s"Standup $standUpName finished"))
      standupRepository.stop(standUpName)
      cancellable.cancel()
      self ! PoisonPill
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing admin connection")
  }
}

object StandupAdminCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandUpRepository) = Props(new StandupAdminCountdownServiceActor(out, standupName, standupRepo: StandUpRepository))
}
