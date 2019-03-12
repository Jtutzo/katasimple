package com.jtutzo.katasimple.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.jtutzo.katasimple.api.CreateUserDTO
import com.jtutzo.katasimple.domaine.*
import java.util.*

data class UserTestEntity(val id: UUID, var username: String, var email: String, var teamId: UUID? = null)

fun jeremyTutzo(): UserTestEntity = UserTestEntity(
        id = UUID.fromString("86e13798-3d13-11e9-b210-d663bd873d93"),
        username = "jeremy.tutzo",
        email = "jtutzo@xebia.fr")

fun francescaCorbella(): UserTestEntity = UserTestEntity(
        id = UUID.fromString("31962bf4-3d22-11e9-b210-d663bd873d93"),
        username = "francesca.corbella",
        email = "fc@email.fr")

fun UserTestEntity.toUserProject(): UserProjection = UserProjection(this.id, this.username, this.email, this.teamId)

fun UserTestEntity.toCreateUserCmd(): CreateUserCommand = CreateUserCommand(this.id, this.username, this.email)

fun UserTestEntity.toUpdateUserCmd(): UpdateUserCommand = UpdateUserCommand(this.id, this.username, this.email, this.teamId)

fun UserTestEntity.toUserCreatedEvent(): UserCreatedEvent = UserCreatedEvent(this.id, this.username, this.email, this.teamId)

fun UserTestEntity.toUserUpdatedEvent(): UserUpdatedEvent = UserUpdatedEvent(this.id, this.username, this.email, this.teamId)

fun UserTestEntity.toCreateUserDTO(): CreateUserDTO = CreateUserDTO(this.username, this.email, this.teamId)

fun Any.toJson() = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
