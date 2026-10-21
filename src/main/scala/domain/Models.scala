package dev.tcoustic
package domain

case class User(id: Long, username: String, role: String)
case class CreateUserRequest(username: String, role: String)
case class ErrorMsg(error: String)
