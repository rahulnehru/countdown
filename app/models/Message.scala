package models

import play.api.libs.json.{Format, Json}

case class Message(message: String)
case object Message {
  implicit val messageFormat: Format[Message] = Json.format[Message]
}