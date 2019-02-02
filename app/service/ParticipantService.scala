package service

import com.google.inject.{Inject, Singleton}
import model.TeamDto
import repository.ParticipantRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ParticipantService @Inject()(repo: ParticipantRepository, implicit val ec: ExecutionContext) {

  def getParticipantsForTeam(team: String): Future[List[TeamDto]] =
    actionParticipantRepo(team)(Future())


  def addParticipants(team: String, participant: String): Future[List[TeamDto]] =
    actionParticipantRepo(team)(repo.insert(participant, team))


  def dropParticipant(team: String, participant: String): Future[List[TeamDto]] =
    actionParticipantRepo(team)(repo.drop(participant, team))


  private def actionParticipantRepo(team: String)(f: Future[_]): Future[List[TeamDto]] = {
    for(_ <- f;
        ps <- repo.getByTeam(team)
    ) yield ps.groupBy(_.teamName).map(t => TeamDto(t._1, t._2.map(_.name).toList)).toList
  }

}
