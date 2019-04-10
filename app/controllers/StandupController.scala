package controllers

import actors.{StandupAdminCountdownServiceActor, StandupClientCountdownServiceActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import models.{Standup, StandupNames}
import play.api.libs.json.{Format, JsValue, Json, OWrites}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import repository.{JsonFileStandupRepository, StandupRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class StandupController @Inject()(cc: ControllerComponents, standupRepo: JsonFileStandupRepository)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  implicit val transformer: MessageFlowTransformer[String, JsValue] = MessageFlowTransformer.jsonMessageFlowTransformer[String, JsValue]
  implicit val standupFormats: Format[Standup] = Standup.formats
  implicit val standupNamesWrites: OWrites[StandupNames] = Json.writes[StandupNames]

  private def isInProgress(standupName: String): Boolean = standupRepo.status(standupName).exists(_.countdown.remaining() >= 0)

  def getAllStandups: Action[AnyContent] = Action {
    Ok(Json.toJson(standupRepo.getAll.map(s => StandupNames(s.name, s.displayName))))
  }

  def get(standupName: String): Action[AnyContent] = Action.async { request =>
    withExistingStandupFromRequest(request) { s => Future.successful(Ok(Json.toJson(s))) }
    standupRepo.get(standupName).fold(
      Future.successful(NotFound(s"Standup with name: [$standupName] doesn't exist"))
    ) {
      s => Future.successful(Ok(Json.toJson(s)))
    }
  }

  def start(standupName: String): WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef[String, JsValue] ( out =>
      StandupAdminCountdownServiceActor.props(out, standupName, standupRepo)
    ).map(_.toString())
  }

  def pause(standupName: String): Action[AnyContent] = Action(Ok(s"paused $standupName"))

  def status(standupName: String): WebSocket = WebSocket.accept[String, String] { request =>
    println("Client connected")
    ActorFlow.actorRef[String, JsValue] ( out =>
      StandupClientCountdownServiceActor.props(out, standupName, standupRepo)
    ).map(_.toString())
  }

  def isStandupLive(standupName: String): Action[AnyContent] = Action.async { req =>
    withExistingStandupFromName(standupName) { s =>
      Future.successful( if (isInProgress(s.name)) Ok else Gone)
    }
  }

  def add: Action[AnyContent] = Action.async { request =>
    withStandupFromRequest(request) { standup =>
      standupRepo.add(standup).map(s => Created(Json.toJson(s)))
    }
  }

  def edit: Action[AnyContent] = Action.async { request =>
    withExistingStandupFromRequest(request) { standup =>
      standupRepo.edit(standup).map(s => Ok(Json.toJson(s)))
    }
  }

  def remove: Action[AnyContent] = Action.async { request =>
    withExistingStandupFromRequest(request) { standup =>
      standupRepo.delete(standup).map(s => Ok("Standup deleted"))
    }
  }

  private def withExistingStandupFromName(name: String)(f: Standup => Future[Result]): Future[Result] = {
    standupRepo.get(name)
      .fold(
          Future.successful(NotFound(s"Standup with name: [$name] doesn't exist"))
      )(
        f
      )
  }

  private def withExistingStandupFromRequest(req: Request[AnyContent])(f: Standup => Future[Result]): Future[Result] = {
    withStandupFromRequest(req) { standup =>
      withExistingStandupFromName(standup.name)(f)
    }
  }

  private def withStandupFromRequest(req: Request[AnyContent])(f: Standup => Future[Result]): Future[Result] = {
    req.body.asJson match {
      case Some(jsVal) =>
        Try(Json.parse(jsVal.toString).as[Standup]) match {
          case Success(s) => f(s)
          case Failure(s) => Future.successful(UnprocessableEntity("Incorrect format of standup: " + s.getMessage))
        }
      case None => Future.successful(BadRequest("Nothing in body / not JSON format"))
    }
  }
}