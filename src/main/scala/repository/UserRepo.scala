package dev.tcoustic
package repository

import cats.effect.Ref
import cats.effect.IO
import domain.{CreateUserRequest, User}

import repository.UserRepo.ActivationResult
import repository.UserRepo.DeactivationResult

object UserRepo:
  sealed trait ActivationResult
  object ActivationResult:
    case object UserNotFound extends ActivationResult
    case object UserAlreadyActive extends ActivationResult
    case class UserActivated(user: User) extends ActivationResult
  sealed trait DeactivationResult
  object DeactivationResult:
    case object UserNotFound extends DeactivationResult
    case object UserAlreadyInactive extends DeactivationResult
    case class UserDeactivated(user: User) extends DeactivationResult

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
  
  def activate(id: Long): IO[ActivationResult] =
    state.modify { case (nextId, users) =>
      users.get(id).map { user =>
        if user.isActive then
          ((nextId, users), ActivationResult.UserAlreadyActive)
        else
          val updatedUser = user.copy(isActive = true)
          ((nextId, users + (id -> updatedUser)), ActivationResult.UserActivated(user))
      }.getOrElse((nextId, users), ActivationResult.UserNotFound)
    }
    
  def deactivate(id: Long): IO[DeactivationResult] =
    state.modify { case (nextId, users) =>
      users.get(id).map { user =>
        if !user.isActive then
          ((nextId, users), DeactivationResult.UserAlreadyInactive)
        else
          val updatedUser = user.copy(isActive = false)
          ((nextId, users + (id -> updatedUser)), DeactivationResult.UserDeactivated(user))
      }.getOrElse((nextId, users), DeactivationResult.UserNotFound)
    }
