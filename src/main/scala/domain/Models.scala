package dev.tcoustic
package domain

case class User(
                 id: Long, 
                 username: String, 
                 role: String,
                 isActive: Boolean = true)
case class CreateUserRequest(username: String, role: String)
