package dev.tcoustic
package domain

sealed trait AppError derives io.circe.Codec.AsObject:
  def message: String

object AppError:
  case class NotFound(message: String) extends AppError
  case class BadRequest(message: String) extends AppError
  case class UnprocessableEntity(message: String) extends AppError