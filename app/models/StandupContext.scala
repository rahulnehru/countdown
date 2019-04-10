package models

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.util.Try

case class StandupContext(standupName: String, standups: List[Standup]){

  private val queue: mutable.Queue[TeamUpdate] =
    standups.find(_.name == standupName).map { standup =>
      mutable.Queue[TeamUpdate](standup.teams.map(t => TeamUpdate(t, new Countdown(t.allocationInSeconds))).toList: _*)
    }.getOrElse(Queue.empty[TeamUpdate])

  private var current: Option[TeamUpdate] = None

  private def next(): Option[TeamUpdate] = Try {
    val n = queue.dequeue()
    current = Some(n)
    n
  }.toOption

  def inProgress(): Option[TeamUpdate] = current

  def left() = List(queue)

  def pause(): Option[TeamUpdate] = {
    current.foreach(_.countdown.pause())
    current
  }

  def unpause(): Option[TeamUpdate] = {
    current.foreach(_.countdown.unpause())
    current
  }

  def startNext(): Option[TeamUpdate] = {
    current = next()
    current.foreach(_.countdown.start())
    current
  }

  def timeLeft(): Long = current.map(_.countdown.remaining()).getOrElse(0L)
}