package controllers

import javax.inject._
import model.TeamDto
import play.api.libs.json.Json
import play.api.mvc._
import service.TeamService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamController @Inject()(cc: ControllerComponents, teamService: TeamService, implicit val executionContext: ExecutionContext) extends AbstractController(cc) {

  implicit val writes = Json.writes[TeamDto]


  def getTeams: Action[AnyContent] = Action.async { implicit r =>
    teamService.getTeamNames map {t => Ok(Json.toJson(t))}
  }

  def addTeam(team: String): Action[AnyContent] = Action.async(implicit r =>
    teamService.addTeam(team) map {t => Ok(Json.toJson(t))}
  )

  def dropTeam(team: String): Action[AnyContent] = Action.async(implicit r =>
    teamService.removeTeam(team) map {t => Ok(Json.toJson(t))}
  )
}
