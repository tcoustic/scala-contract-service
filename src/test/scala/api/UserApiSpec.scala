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
      initialState <- Ref.of[IO, (Long, Map[Long, User])]((5L, Map(
        1L -> User(1L, "alice_admin", "ADMIN"),
        2L -> User(2L, "bob_tester", "QA", isActive = false),
        3L -> User(3L, "charlie_admin", "ADMIN", isActive = false),
        4L -> User(4L, "dave_tester", "QA")
      )))
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
      _ = assertEquals(users.size, 4)
      _ = assertEquals(users.head.username, "alice_admin")
    yield ()
  }

  test("GET /api/users?isActive=true should return only active users"):
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users?isActive=true")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assertEquals(users.size, 2)
      _ = assertEquals(users.map(_.username), List("alice_admin", "dave_tester"))
    yield ()

  test("GET /api/users?role=ADMIN should return only admin users"):
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users?role=ADMIN")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assertEquals(users.size, 2)
      _ = assertEquals(users.map(_.username), List("alice_admin", "charlie_admin"))
    yield ()

  test("GET /api/users?isActive=false&role=QA should filter by multiple parameters"):
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users?isActive=false&role=QA")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assertEquals(users.size, 1)
      _ = assertEquals(users.head.username, "bob_tester")
    yield ()

  test("GET /api/users with take and skip should paginate correctly"):
    for
      routes <- testRoutes
      request = Request[IO](Method.GET, uri"/api/users?take=2&skip=1")
      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      users <- response.as[List[User]]

      _ = assertEquals(users.size, 2)
      _ = assertEquals(users.map(_.username), List("bob_tester", "charlie_admin"))
    yield ()

  test("POST /api/users should auto-generate an ID and return the created user") {
    for
      routes <- testRoutes
      createReq = CreateUserRequest("eva_dev", "DEVELOPER")
      request = Request[IO](Method.POST, uri"/api/users")
        .withEntity(createReq.asJson)

      response <- routes.orNotFound.run(request)

      _ = assertEquals(response.status, Status.Ok)
      createdUser <- response.as[User]

      _ = assertEquals(createdUser.id, 5L)
      _ = assertEquals(createdUser.username, "eva_dev")
      _ = assertEquals(createdUser.role, "DEVELOPER")
    yield ()
  }

  case class LifecycleTestCase(
    name: String,
    path: String,
    expectedStatus: Status
  )

  val lifecycleTestCases: Seq[LifecycleTestCase] = List(
    LifecycleTestCase(
      name = "POST to /activate successfully activates inactive user (ID 2)",
      path = "/api/users/2/activate",
      expectedStatus = Status.Ok
    ),
    LifecycleTestCase(
      name = "POST to /deactivate successfully deactivates active user (ID 1)",
      path = "/api/users/1/deactivate",
      expectedStatus = Status.Ok
    ),
    LifecycleTestCase(
      name = "POST to /activate fails to activate already active user (ID 1)",
      path = "/api/users/1/activate",
      expectedStatus = Status.UnprocessableContent
    ),
    LifecycleTestCase(
      name = "POST to /deactivate fails to deactivate already inactive user (ID 2)",
      path = "/api/users/2/deactivate",
      expectedStatus = Status.UnprocessableContent
    ),
    LifecycleTestCase(
      name = "POST to /activate fails to activate non-existent user (ID 999)",
      path = "/api/users/999/activate",
      expectedStatus = Status.NotFound
    ),
    LifecycleTestCase(
      name = "POST to /deactivate fails to deactivate non-existent user (ID 999)",
      path = "/api/users/999/deactivate",
      expectedStatus = Status.NotFound
    )
  )

  lifecycleTestCases.foreach { tc =>
    test(tc.name):
      for
        routes <- testRoutes
        req = Request[IO](Method.POST, Uri.unsafeFromString(tc.path))
        resp <- routes.orNotFound.run(req)
        _ <- IO(assertEquals(resp.status, tc.expectedStatus))
      yield ()
  }