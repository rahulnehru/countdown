package models

import play.api.libs.json.{Json, Writes}

case class TeamUpdate(team: Team, countdown: Countdown)

object TeamUpdate {
  implicit val teamUpdateWrites: Writes[TeamUpdate] = (update: TeamUpdate) => {
    Json.obj(
      "name" -> update.team.name,
      "name" -> update.team.name,
      "speaker" -> update.team.speaker,
      "remainingSeconds" -> update.countdown.remaining()
    )
  }

  //implicit val teamUpdateFormat = Json.format[TeamUpdate]
}