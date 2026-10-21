package dev.tcoustic
package api

import cats.effect.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import domain.{CreateUserRequest, User}
import repository.UserRepo

import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

class UserApiSpec extends CatsEffectSuite:

  private def testRoutes: IO[HttpRoutes[IO]] =
    for
      initialState <- Ref.of[IO, (Long, Map[Long, User])]((2L, Map(1L -> User(1L, "alice_admin", "ADMIN"))))
      repo = UserRepo(initialState)
    yield UserRoutes.make(repo)

  test("GET /api/users/{id} should return user when ID exists") {
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users/1")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      body <- response.as[User]
      _ = assertEquals(body.username, "alice_admin")
      _ = assertEquals(body.role, "ADMIN")
    yield ()
  }

  test("GET /api/users/{id} should return 404/NotFound when user does not exist") {
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users/999")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.NotFound)
    yield ()
  }

  test("GET /api/users/{id} should return 400/BadRequest when id is not a number") {
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users/abc")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.BadRequest)
    yield ()
  }

  test("GET /api/users should return users as a list") {
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assert(users.nonEmpty)
      _ = assertEquals(users.size, 1)
      _ = assertEquals(users.head.username, "alice_admin")
    yield ()
  }

  test("GET /api/users should return all users when there is a longer list") {
    val expectedUsers = List(
      User(1L, "alice_admin", "ADMIN"),
      User(2L, "bob_tester", "QA"),
      User(3L, "charlie_dev", "DEVELOPER"),
      User(4L, "david_pm", "PRODUCT"),
      User(5L, "eva_designer", "DESIGN")
    )
    val stateMap = expectedUsers.map(u => u.id -> u).toMap
    for
      initialState <- Ref.of[IO, (Long, Map[Long, User])]((6L, stateMap))
      repo = UserRepo(initialState)
      routes = UserRoutes.make(repo)
      request = Request[IO](Method.GET, uri"/api/users")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assertEquals(users.length, expectedUsers.length)
      _ = assertEquals(users.sortBy(_.id), expectedUsers)
    yield ()
  }

  test("POST /api/users should auto-generate an ID and return the created user") {
    for
      routes <- testRoutes
      createReq = CreateUserRequest("bob_tester", "QA")
      request = Request[IO](Method.POST, uri"/api/users")
        .withEntity(createReq.asJson)

      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      createdUser <- response.as[User]

      _ = assertEquals(createdUser.id, 2L)
      _ = assertEquals(createdUser.username, "bob_tester")
      _ = assertEquals(createdUser.role, "QA")
    yield ()
  }