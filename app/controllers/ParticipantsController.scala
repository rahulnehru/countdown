package controllers

import javax.inject._
import model.TeamDto
import play.api.libs.json.Json
import play.api.mvc._
import service.ParticipantService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ParticipantsController @Inject()(cc: ControllerComponents, participantService: ParticipantService, implicit val executionContext: ExecutionContext) extends AbstractController(cc) {

  implicit val writes = Json.writes[TeamDto]

  def getParticipants(team: String): Action[AnyContent] = Action.async { implicit r =>
    Future(Ok(Json.toJson(participantService.getParticipants(team))))
  }

  def addParticipant(team: String, participant: String): Action[AnyContent] = Action.async(implicit r =>
    Future {
      Ok(Json.toJson(participantService.addParticipants(team, participant)))
    }
  )

  def removeParticipant(team: String, participant: String): Action[AnyContent] = Action.async(implicit r =>
    Future{
      Ok(Json.toJson(participantService.removeParticipant(team, participant)))
    }
  )
}
