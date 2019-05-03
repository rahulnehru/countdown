package controllers

import actors.{StandUpClientCountdownServiceActor, StandupAdminCountdownServiceActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import models.{Standup, StandupNames, Team}
import play.api.Logging
import play.api.libs.json.{Format, JsValue, Json, OWrites}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import repository.StandUpRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class StandupController @Inject()(cc: ControllerComponents, standupRepo: StandUpRepository)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) with Logging {

  implicit val transformer: MessageFlowTransformer[String, JsValue] = MessageFlowTransformer.jsonMessageFlowTransformer[String, JsValue]
  implicit val standUpFormats: Format[Standup] = Standup.formats
  implicit val standUpNamesWrites: OWrites[StandupNames] = Json.writes[StandupNames]

  private def isInProgress(standUpName: String): Boolean = standupRepo.status(standUpName).exists(_.countdown.remaining() >= 0)

  def getAllStandUps: Action[AnyContent] = Action.async {
    standupRepo
      .getAll
      .map(standUps => Ok(Json.toJson(standUps.map(s => StandupNames(s.name, s.displayName)))))
  }

  def get(standUpName: String): Action[AnyContent] = Action.async { request =>
    standupRepo.find(standUpName).map(_.fold(
      NotFound(s"Stand up with name: [$standUpName] doesn't exist")
    )(s => Ok(Json.toJson(s))))
  }

  def start(standUpName: String): WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef[String, JsValue](out =>
      StandupAdminCountdownServiceActor.props(out, standUpName, standupRepo)
    ).map(_.toString())
  }

  def pause(standUpName: String): Action[AnyContent] = Action(Ok(s"paused $standUpName"))

  def status(standUpName: String): WebSocket = WebSocket.accept[String, String] { request =>
    logger.info("Client connected")
    ActorFlow.actorRef[String, JsValue](out =>
      StandUpClientCountdownServiceActor.props(out, standUpName, standupRepo)
    ).map(_.toString())
  }

  def isStandUpLive(standUpName: String): Action[AnyContent] = Action.async { req =>
    withExistingStandUpFromName(standUpName) { s =>
      Future.successful(if (isInProgress(s.name)) Ok else Gone)
    }
  }

  def add: Action[AnyContent] = Action.async { request =>
    withStandUpFromRequest(request) { standUp =>
      for {
        isTaken  <- standupRepo.find(standUp.name).map(_.exists(_.name == standUp.name))
        response <- if (isTaken)
          Future.successful(Conflict(s"Standup name: ${standUp.name} is taken"))
        else
          standupRepo.add(standUp).map(s => Created(Json.toJson(s)))
      } yield response
    }
  }

  def addTeams(standUpName: String) = Action.async { request =>
    standupRepo
      .addTeams(standUpName, request.body.asJson.get.as[Set[Team]])
      .map(rows => Ok(s"$rows teams added to $standUpName standUp"))
      .recover{
        case NonFatal(_) => BadRequest(s"Teams cannot be added to $standUpName")
      }
  }

  def removeTeams() = Action.async { request =>
    val teamsToBeRemoved = request.body.asJson.get.as[Set[String]]
    logger.info(s"Removing teams $teamsToBeRemoved")
    standupRepo
      .removeTeams(teamsToBeRemoved)
      .map(rows => Ok(s"$rows teams removed"))
      .recover{
        case NonFatal(_) => BadRequest(s"Teams cannot be removed to $teamsToBeRemoved")
      }
  }

  def edit: Action[AnyContent] = Action.async { request =>
    withStandUpFromRequest(request) { standUp =>
      standupRepo.edit(standUp).map(s => Ok(Json.toJson(s)))
    }
  }

  def removeStandUp(standUpName: String): Action[AnyContent] = Action.async { _ =>
    withExistingStandUpFromName(standUpName) { standUp =>
      standupRepo.delete(standUp).map(_ => Ok(s"StandUp $standUpName deleted"))
    }
  }

  private def withExistingStandUpFromName(name: String)(f: Standup => Future[Result]): Future[Result] = {
    standupRepo
      .find(name)
      .flatMap(_.fold(Future.successful(NotFound(s"Stand up with name: [$name] doesn't exist")))(f))
  }

  private def withStandUpFromRequest(req: Request[AnyContent])(f: Standup => Future[Result]): Future[Result] = {
    req.body.asJson match {
      case Some(jsVal) =>
        Try(Json.parse(jsVal.toString).as[Standup]) match {
          case Success(s) => f(s)
          case Failure(s) => Future.successful(UnprocessableEntity("Incorrect format of standUp: " + s.getMessage))
        }
      case None => Future.successful(BadRequest("Nothing in body / not JSON format"))
    }
  }


}