package controllers

import actors.{StandupAdminCountdownServiceActor, StandupClientCountdownServiceActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import repository.InMemoryStandupRepository


class StandupController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  implicit val transformer = MessageFlowTransformer.jsonMessageFlowTransformer[String, JsValue]

  private def isInProgress(standupName: String) = standupRepo.status(standupName).exists(_.countdown.remaining() >= 0)

    //TODO inject it
  val standupRepo = new InMemoryStandupRepository()

  def allStandups = Action {
    Ok(Json.toJson(standupRepo.standups.map(s => Json.toJsObject(Map(
        "name" -> s.name,
        "displayName" -> s.displayName
      ))
    )))
  }

  def standup(standupName: String) = Action {
    standupRepo.standups.find(_.name == standupName).fold(NotFound(s"No standup with name $standupName"))(s => Ok(Json.toJson(s)))
  }

  def start(standupName: String) = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef[String, JsValue] ( out =>
      StandupAdminCountdownServiceActor.props(out, standupName, standupRepo)
    ).map(_.toString())
  }

  def pause(standupName: String) = Action(Ok(s"paused $standupName"))

  def status(standupName: String) = WebSocket.accept[String, String] { request =>
    println("Client connected")
    ActorFlow.actorRef[String, JsValue] ( out =>
      StandupClientCountdownServiceActor.props(out, standupName, standupRepo)
    ).map(_.toString())
  }

  def isStandupLive(standupName: String): Action[AnyContent] = Action {
    standupRepo.standups.find(_.name == standupName)
      .fold(NotFound(s"No standup with name $standupName"))(s => if (isInProgress(s.name)) Ok else Gone)
  }

}