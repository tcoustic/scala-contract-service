package dev.tcoustic
package api

import cats.effect.*
import cats.syntax.all.*
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import repository.UserRepo
import domain.ErrorMsg

import sttp.model.StatusCode

object UserRoutes:

  private def notFound(id: Long): Left[(StatusCode, ErrorMsg), Nothing] =
    Left((StatusCode.NotFound, ErrorMsg(s"User with id $id not found")))
    
  def make(repo: UserRepo): HttpRoutes[IO] =
    val getUserRoute = UserEndpoints.getUserEndpoint.serverLogic { id =>
      repo.find(id).map {
        case Some(user) => Right(user)
        case None       => notFound(id)
      }
    }

    val listUsersRoute = UserEndpoints.listUsersEndpoint.serverLogic { _ =>
      repo.listAll().map(Right(_))
    }

    val createUserRoute = UserEndpoints.createUserEndpoint.serverLogic { req =>
      repo.create(req).map(Right(_))
    }

    val deactivateUserRoute = UserEndpoints.deactivateUserEndpoint.serverLogic { id =>
      repo.deactivate(id).map {
        case UserRepo.DeactivationResult.UserDeactivated(user) => Right(user)
        case UserRepo.DeactivationResult.UserAlreadyInactive => Left((StatusCode.UnprocessableEntity, ErrorMsg(s"User with id $id already inactive")))
        case UserRepo.DeactivationResult.UserNotFound => notFound(id)
      }
    }

    val activateUserRoute = UserEndpoints.activateUserEndpoint.serverLogic { id =>
      repo.activate(id).map {
        case UserRepo.ActivationResult.UserActivated(user) => Right(user)
        case UserRepo.ActivationResult.UserAlreadyActive => Left((StatusCode.UnprocessableEntity, ErrorMsg(s"User with id $id already active")))
        case UserRepo.ActivationResult.UserNotFound => notFound(id)
      }
    }

    val swaggerRoutes = Http4sServerInterpreter[IO]().toRoutes(
      SwaggerInterpreter().fromEndpoints(
        List(
          UserEndpoints.getUserEndpoint,
          UserEndpoints.listUsersEndpoint,
          UserEndpoints.createUserEndpoint,
          UserEndpoints.deactivateUserEndpoint,
          UserEndpoints.activateUserEndpoint),
        "User API",
        "1.0.0"
      )
    )

    Http4sServerInterpreter[IO]().toRoutes(
      List(
        getUserRoute, 
        listUsersRoute, 
        createUserRoute,
        deactivateUserRoute,
        activateUserRoute)
    ) <+> swaggerRoutes
    
    
