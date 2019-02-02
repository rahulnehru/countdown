package controllers

import javax.inject._
import model.TeamDto
import play.api.Logging
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import service.ParticipantService

import scala.concurrent.ExecutionContext

@Singleton
class ParticipantsController @Inject()(cc: ControllerComponents, participantService: ParticipantService, implicit val executionContext: ExecutionContext) extends AbstractController(cc) with Logging  {

  implicit val writes: OWrites[TeamDto] = Json.writes[TeamDto]

  def getParticipants(team: String): Action[AnyContent] = Action.async { implicit r =>
    logger.info(s"Adding team [$team]")
    participantService.getParticipantsForTeam(team) map toJsonResponse
  }

  def addParticipant(team: String, participant: String): Action[AnyContent] = Action.async { implicit r =>
    participantService.addParticipants(team, participant) map toJsonResponse
  }

  def dropParticipant(team: String, participant: String): Action[AnyContent] = Action.async { implicit r =>
    participantService.dropParticipant(team, participant) map toJsonResponse
  }

  private def toJsonResponse: Seq[TeamDto] => Result = p => Ok(Json.toJson(p))
}
