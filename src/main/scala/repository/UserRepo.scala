package dev.tcoustic
package repository

import cats.effect.Ref
import cats.effect.IO
import domain.{User, CreateUserRequest}

class UserRepo(state: Ref[IO, (Long, Map[Long, User])]):

  def find(id: Long): IO[Option[User]] =
    state.get.map { case (_, users) =>
      users.get(id)
    }

  def listAll(): IO[List[User]] =
    state.get.map { case (_, users) =>
      users.values.toList
    }

  def create(req: CreateUserRequest): IO[User] =
    state.modify { case (nextId, users) =>
      val newUser = User(nextId, req.username, req.role)
      ((nextId + 1, users + (nextId -> newUser)), newUser)
    }