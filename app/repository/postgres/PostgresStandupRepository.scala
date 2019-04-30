package repository.postgres


import javax.inject.{Inject, Singleton}
import models.Standup
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.StandupRepository

import scala.concurrent.Future

@Singleton
class PostgresStandupRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends StandupRepository with HasDatabaseConfigProvider[CountdownPostgresDriver] {

  override def get(standupName: String): Option[Standup] = ???

  override def add(standup: Standup): Future[Standup] = ???

  override def edit(standup: Standup): Future[Standup] = ???

  override def delete(standup: Standup): Future[Boolean] = ???

  override def getAll: List[Standup] = ???
}