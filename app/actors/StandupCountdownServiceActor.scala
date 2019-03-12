package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.countdown._
import play.api.libs.json.Json.toJson
import repository.StandupRepository

class StandupCountdownServiceActor(out: ActorRef, standupName: String, standupRepository: StandupRepository) extends Actor {

  override def receive: Receive = {
    case "start" =>
      if( !standupRepository.standups.exists(_.name == standupName)){
        out ! toJson(Message(s"No such $standupName standup exist!!!"))
        self ! PoisonPill
      } else {
        standupRepository.status(standupName).fold {
          standupRepository.start(standupName)
          context.become(started)
          out ! toJson(standupRepository.status(standupName))
        }{_ =>
          context.become(started)
          self ! s"status"
        }
      }

    case "status" =>
      standupRepository.status(standupName).fold {
        out ! toJson(Message(s"Standup $standupName is not in progress"))
      }{_ =>
        context.become(started)
        self ! "status"
      }

    case "exit"|"close" =>
      context.become(started)
      self ! "exit"
  }

  def started: Receive = {
    case "status" =>
      out ! toJson(standupRepository.status(standupName))

    case "next" =>
      standupRepository.next(standupName).fold(self ! "stop")(ip => out ! toJson(ip))

    case "pause" => out ! toJson(standupRepository.pause(standupName))

    case "stop" =>
      out ! toJson(Message(s"Standup $standupName finished"))
      standupRepository.stop(standupName)
      self ! PoisonPill

    case "exit"|"close" =>
      out ! toJson(Message(s"Exiting. Standup may already be running"))
      self ! PoisonPill

    case "start" => out ! toJson(Message(s"Standup $standupName is already in progress"))
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Closing connection")
  }
}

object StandupCountdownServiceActor {
  def props(out: ActorRef, standupName: String, standupRepo: StandupRepository) = Props(new StandupCountdownServiceActor(out, standupName, standupRepo: StandupRepository))
  case class Next(team: Team, queue: List[Team], out: ActorRef)
  case object Done//not sure if out needed
}
