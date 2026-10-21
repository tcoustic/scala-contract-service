package dev.tcoustic

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import domain.User
import repository.UserRepo
import api.UserRoutes

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      initialState <- Ref.of[IO, (Long, Map[Long, User])](
        (2L, Map(1L -> User(1L, "qa_lead_alice", "ADMIN")))
      )
      repo = UserRepo(initialState)
      routes = UserRoutes.make(repo)

      _ <- EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(routes.orNotFound)
        .build
        .use(_ => IO.never)
    yield ExitCode.Success