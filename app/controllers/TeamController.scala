package controllers

import javax.inject._
import model.TeamDto
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import service.TeamService

import scala.concurrent.ExecutionContext

@Singleton
class TeamController @Inject()(cc: ControllerComponents, teamService: TeamService, implicit val executionContext: ExecutionContext) extends AbstractController(cc) {

  implicit val writes: OWrites[TeamDto] = Json.writes[TeamDto]


  def getTeams: Action[AnyContent] = Action.async { implicit r =>
    teamService.getTeamNames map toJsonResponse
  }

  def addTeam(team: String): Action[AnyContent] = Action.async(implicit r =>
    teamService.addTeam(team) map toJsonResponse
  )

  def dropTeam(team: String): Action[AnyContent] = Action.async(implicit r =>
    teamService.removeTeam(team) map toJsonResponse
  )

  private def toJsonResponse: Seq[String] => Result = p => Ok(Json.toJson(p))
}
