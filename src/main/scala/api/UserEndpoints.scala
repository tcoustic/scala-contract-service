package dev.tcoustic
package api

import domain.{CreateUserRequest, ErrorMsg, User}

import sttp.tapir.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object UserEndpoints:
  val getUserEndpoint: PublicEndpoint[Long, ErrorMsg, User, Any] = endpoint
    .get
    .in("api" / "users" / path[Long]("id"))
    .out(jsonBody[User])
    .errorOut(jsonBody[ErrorMsg])
    .description("Find user by ID")

  val createUserEndpoint: PublicEndpoint[CreateUserRequest, Unit, User, Any] = endpoint
    .post
    .in("api" / "users")
    .in(jsonBody[CreateUserRequest])
    .out(jsonBody[User])
    .description("Create a new user")
