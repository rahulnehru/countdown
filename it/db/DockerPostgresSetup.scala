package db

import java.sql.DriverManager

import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerFactory, DockerKit, DockerReadyChecker}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

trait DockerPostgresSetup extends DockerKit {
  lazy val hostPort = 44444
  //lazy val hostPort = 2000 + scala.util.Random.nextInt(8000)
  lazy val containerPort = 5432
  lazy val user = "postgres"
  lazy val password = "safepassword"
  lazy val database = "postgres" //same as user
  lazy val dockerImage = "postgres:10.4"
  lazy val dbUrl = s"jdbc:postgresql://localhost:$hostPort/$database"
  lazy val driver = "org.postgresql.Driver"

  private lazy val client = DefaultDockerClient.fromEnv().build()
  override implicit lazy val dockerFactory: DockerFactory = new SpotifyDockerFactory(client)

  lazy val postgresContainer = DockerContainer(dockerImage)
    .withPorts((containerPort, Some(hostPort)))
    .withEnv(s"POSTGRES_USER=$user", s"POSTGRES_PASSWORD=$password", s"POSTGRES_DB=$database")
    .withReadyChecker(new PostgresReadyChecker(dbUrl, user, password).looped(10, 1.second))

  // adds our container to the DockerKit's list
  abstract override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers
}

class PostgresReadyChecker(dbUrl: String, user: String, password: String) extends DockerReadyChecker {
  lazy val driver = "org.postgresql.Driver"
  override def apply(container: DockerContainerState
                    )(implicit docker: DockerCommandExecutor, ec: ExecutionContext) = {

    container.getPorts().map { _ =>
      Try {
        Class.forName(driver)
        Option(DriverManager.getConnection(dbUrl, user, password))
          .map(_.close)
          .isDefined
      }.getOrElse(false)
    }
  }
}



