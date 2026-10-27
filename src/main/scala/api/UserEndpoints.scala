package dev.tcoustic
package api

import domain.{AppError, CreateUserRequest, User}

import sttp.tapir.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*

object UserEndpoints:

  private val baseEndpoint = endpoint
    .errorOut(
      oneOf[AppError](
        oneOfVariant(StatusCode.NotFound, jsonBody[AppError.NotFound].description("Resource not found")),
        oneOfVariant(StatusCode.BadRequest, jsonBody[AppError.BadRequest].description("Invalid request parameters")),
        oneOfVariant(StatusCode.UnprocessableEntity, jsonBody[AppError.UnprocessableEntity].description("Unable to process the request")),
      )
    )

  val getUserEndpoint: PublicEndpoint[Long, AppError, User, Any] = baseEndpoint
    .get
    .in("api" / "users" / path[Long]("id"))
    .out(jsonBody[User])
    .description("Find user by ID")

  val createUserEndpoint: PublicEndpoint[CreateUserRequest, AppError, User, Any] = baseEndpoint
    .post
    .in("api" / "users")
    .in(jsonBody[CreateUserRequest])
    .out(jsonBody[User])
    .description("Create a new user")

  val listUsersEndpoint: PublicEndpoint[(Option[Boolean], Option[String], Int, Int), AppError, List[User], Any] = baseEndpoint
    .get
    .in("api" / "users")
    .in(query[Option[Boolean]]("isActive"))
    .in(query[Option[String]]("role"))
    .in(query[Int]("take").default(10))
    .in(query[Int]("skip").default(0))
    .out(jsonBody[List[User]])
    .description("List all users")

  val deactivateUserEndpoint: PublicEndpoint[Long, AppError, User, Any] = baseEndpoint
    .post
    .in("api" / "users" / path[Long]("id") / "deactivate")
    .out(jsonBody[User])
    .description("Deactivate a user by ID")

  val activateUserEndpoint: PublicEndpoint[Long, AppError, User, Any] = baseEndpoint
    .post
    .in("api" / "users" / path[Long]("id") / "activate")
    .out(jsonBody[User])
    .description("Activate a user by ID")