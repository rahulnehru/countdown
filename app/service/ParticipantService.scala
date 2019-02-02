package service

import com.google.inject.Singleton
import model.TeamDto

@Singleton
class ParticipantService {

  var participants = List("Steff", "Katie")

  def getParticipants(team: String) = {
    TeamDto(team, participants)
  }

  def addParticipants(team: String, participantsToAdd: String) = {
    participants  = participants :+ participantsToAdd
    TeamDto(team, participants)
  }
  def removeParticipant(team: String, participant: String) = {
    participants = participants.filterNot(_ == participant)
    TeamDto(team, participants)
  }

}
