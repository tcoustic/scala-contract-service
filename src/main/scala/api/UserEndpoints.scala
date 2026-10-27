package dev.tcoustic
package api

import domain.{CreateUserRequest, ErrorMsg, User}

import sttp.tapir.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*

object UserEndpoints:
  val getUserEndpoint: PublicEndpoint[Long, (StatusCode, ErrorMsg), User, Any] = endpoint
    .get
    .in("api" / "users" / path[Long]("id"))
    .out(jsonBody[User])
    .errorOut(statusCode.and(jsonBody[ErrorMsg]))
    .description("Find user by ID")

  val createUserEndpoint: PublicEndpoint[CreateUserRequest, Unit, User, Any] = endpoint
    .post
    .in("api" / "users")
    .in(jsonBody[CreateUserRequest])
    .out(jsonBody[User])
    .description("Create a new user")

  val listUsersEndpoint: PublicEndpoint[Unit, Unit, List[User], Any] = endpoint
    .get
      .in("api" / "users")
      .out(jsonBody[List[User]])
      .description("List all users")

  val deactivateUserEndpoint: PublicEndpoint[Long, (StatusCode, ErrorMsg), User, Any] = endpoint
    .post
    .in("api" / "users" / path[Long]("id") / "deactivate")
    .out(jsonBody[User])
    .errorOut(statusCode.and(jsonBody[ErrorMsg]))
    .description("Deactivate a user by ID")

  val activateUserEndpoint: PublicEndpoint[Long, (StatusCode, ErrorMsg), User, Any] = endpoint
    .post
    .in("api" / "users" / path[Long]("id") / "activate")
    .out(jsonBody[User])
    .errorOut(statusCode.and(jsonBody[ErrorMsg]))
    .description("Activate a user by ID")