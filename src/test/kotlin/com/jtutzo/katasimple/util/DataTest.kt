package com.jtutzo.katasimple.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.jtutzo.katasimple.api.CreateUserDTO
import com.jtutzo.katasimple.domaine.*
import java.util.*

data class UserTestData(val id: UUID, var username: String, var email: String, var teamId: UUID? = null) {
    companion object {
        val USER1 = UserTestData(id = UUID.fromString("86e13798-3d13-11e9-b210-d663bd873d93"), username = "jeremy.tutzo", email = "jtutzo@xebia.fr")
        val USER2 = UserTestData(id = UUID.fromString("31962bf4-3d22-11e9-b210-d663bd873d93"), username = "francesca.corbella", email = "fc@email.fr")
    }

    fun rebuild(username: String = this.username, email: String = this.email, teamId: UUID? = this.teamId) = UserTestData(this.id, this.username, this.email, this.teamId)
}

fun UserTestData.buildUserProject(): UserProjection = UserProjection(this.id, this.username, this.email, this.teamId)

fun UserTestData.buildCreateUserCmd(): CreateUserCommand = CreateUserCommand(this.id, this.username, this.email)

fun UserTestData.buildUpdateUserCmd(): UpdateUserCommand = UpdateUserCommand(this.id, this.username, this.email, this.teamId)

fun UserTestData.buildUserCreatedEvent(): UserCreatedEvent = UserCreatedEvent(this.id, this.username, this.email, this.teamId)

fun UserTestData.buildUserUpdatedEvent(): UserUpdatedEvent = UserUpdatedEvent(this.id, this.username, this.email, this.teamId)

fun UserTestData.buildCreateUserDTO(): CreateUserDTO = CreateUserDTO(this.username, this.email, this.teamId)

fun Any.buildToJson() = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
