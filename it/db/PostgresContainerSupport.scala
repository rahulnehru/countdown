package db

import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Suite}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Mode, Play}

import scala.concurrent.Future

trait PostgresContainerSupport extends DockerPostgresSetup with DockerTestKit  {
  self: Suite =>

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout =  Span(20, Seconds), interval = Span(5, Millis))

  lazy val additionalConfiguration = List(
    "dbplugin" -> "disabled",
    "play.http.secret.key" -> "SomeSecret",
    "play.filters.csrf.token.sign" -> false,
    "buildInfoFile" -> "",
    "slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$",
    "slick.dbs.default.db.driver" -> driver,
    "slick.dbs.default.db.url" -> dbUrl,
    "slick.dbs.default.db.user" -> user,
    "slick.dbs.default.db.password" -> password,
    "evolutionplugin" -> "disabled"
  )

  lazy val fakeapp: Application =
    new GuiceApplicationBuilder().
      in(Mode.Test).
      configure(additionalConfiguration: _*).
  build()

  private lazy val dataSource = new HikariDataSource({
    val hConfig = new HikariConfig
    hConfig.setJdbcUrl(dbUrl)
    hConfig.setUsername(user)
    hConfig.setPassword(password)
    hConfig
  })


  def initialise() = for {
    _ <- Future.successful(super.beforeAll())
    _ <- isContainerReady(postgresContainer)
    _ <- Future.successful(Play.start(fakeapp))
    _ <- Future.successful(Evolutions.applyEvolutions(fakeapp.injector.instanceOf[DBApi].database("default")))
  } yield true

}

trait PostgresContainerSetup extends PostgresContainerSupport with BeforeAndAfterAll with ScalaFutures {
  self: Suite =>

  override def beforeAll() =
    whenReady(initialise()) { result =>
      assert(result == true)
    }

  override def afterAll(): Unit = {
    super.afterAll()
    Play.stop(fakeapp)
  }
}


