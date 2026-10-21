package dev.tcoustic
package api

import cats.effect.*
import cats.syntax.all.*
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import repository.UserRepo

object UserRoutes:
  def make(repo: UserRepo): HttpRoutes[IO] =
    val getUserRoute = UserEndpoints.getUserEndpoint.serverLogic { id =>
      repo.find(id).map {
        case Some(user) => Right(user)
        case None       => Left(dev.tcoustic.domain.ErrorMsg(s"User with id $id not found"))
      }
    }

    val listUsersRoute = UserEndpoints.listUsersEndpoint.serverLogic { _ =>
      repo.listAll().map(Right(_))
    }

    val createUserRoute = UserEndpoints.createUserEndpoint.serverLogic { req =>
      repo.create(req).map(Right(_))
    }

    val swaggerRoutes = Http4sServerInterpreter[IO]().toRoutes(
      SwaggerInterpreter().fromEndpoints(
        List(
          UserEndpoints.getUserEndpoint,
          UserEndpoints.listUsersEndpoint,
          UserEndpoints.createUserEndpoint),
        "User API",
        "1.0.0"
      )
    )

    Http4sServerInterpreter[IO]().toRoutes(
      List(
        getUserRoute, 
        listUsersRoute, 
        createUserRoute)
    ) <+> swaggerRoutes
