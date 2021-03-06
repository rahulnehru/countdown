package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.Message
import play.api.libs.json.Json.toJson
import repository.StandupRepository

import scala.concurrent.duration._

class StandupAdminCountdownServiceActor(out: ActorRef, standupName: String, standupRepository: StandupRepository) extends Actor {


  if( !standupRepository.getAll.exists(_.name == standupName)){
    out ! toJson(Message(s"No such $standupName standup exist!!!"))
    self ! PoisonPill
  } else {
    self ! "start"
  }

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
      standupRepository.start(standupName)
      context.become(connected)
    case "join" =>
      println(s"${this.getClass.getCanonicalName} - Joined standup")
      println(standupName)
      context.become(connected)
    case "status" =>
      out ! toJson(standupRepository.status(standupName))
    case "next" =>
      println("Next person")
      standupRepository.next(standupName).fold(self ! "stop")(_ => self ! "status")
    case "unpause" =>
      println("Unpause / continue")
      standupRepository.unpause(standupName)
      self ! "status"
    case "pause" =>
      println("Pause")
      out ! toJson(Message(s"Speaker $standupName paused"))
      standupRepository.pause(standupName)
      self ! "status"
    case "stop" =>
      out ! toJson(Message(s"Standup $standupName finished"))
      standupRepository.stop(standupName)
      cancellable.cancel()
      self ! PoisonPill
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing admin connection")
  }
}

object StandupAdminCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandupRepository) = Props(new StandupAdminCountdownServiceActor(out, standupName, standupRepo: StandupRepository))
}
