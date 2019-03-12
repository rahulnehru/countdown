package controllers

import actors.StandupCountdownServiceActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import repository.InMemoryStandupRepository


class StandupController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  implicit val transformer = MessageFlowTransformer.jsonMessageFlowTransformer[String, JsValue]

    //TODO inject it
  val standupRepo = new InMemoryStandupRepository()

  def allStandups = Action {
    Ok(Json.toJson(standupRepo.standups.map(_.name)))
  }

  def standup(standupName: String) = Action {
    standupRepo.standups.find(_.name == standupName).fold(NotFound(s"No standup with name $standupName"))(s => Ok(Json.toJson(s)))
  }

  def connect(standupName: String) = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef[String, JsValue] ( out =>
      StandupCountdownServiceActor.props(out, standupName, standupRepo)
    ).map(_.toString())
  }

  def pause(standupName: String) = Action(Ok(s"paused $standupName"))

  def status(standupName: String) = Action(Ok(s"paused $standupName"))

}