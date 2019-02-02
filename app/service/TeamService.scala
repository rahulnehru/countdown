package service

import com.google.inject.{Inject, Singleton}
import repository.TeamRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamService @Inject()(repo: TeamRepository, implicit val ec: ExecutionContext) {

  def getTeamNames: Future[List[String]] = actionTeamRepo(Future())

  def addTeam(team: String): Future[List[String]] = actionTeamRepo(repo.insert(team))

  def removeTeam(team: String): Future[List[String]] = actionTeamRepo(repo.drop(team))

  private def actionTeamRepo(f: Future[_]): Future[List[String]] =
    for(_ <- f; teams <- repo.all()) yield teams.map(_.name).toList

}
