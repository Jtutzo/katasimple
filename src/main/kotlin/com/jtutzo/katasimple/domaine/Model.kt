package com.jtutzo.katasimple.domaine

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class CreateUserCommand(val id: UUID, val username: String, val email: String, val teamId: UUID? = null)
data class UpdateUserCommand(@TargetAggregateIdentifier val id: UUID, val username: String, val email: String, val teamId: UUID?)
data class UserCreatedEvent(val id: UUID, val username: String, val email: String, val teamId: UUID?)
data class UserUpdatedEvent(val id: UUID, val username: String, val email: String, val teamId: UUID?)
data class UserProjection(val id: UUID, val username: String, val email: String, val teamId: UUID? = null)

fun CreateUserCommand.buildEvent() = UserCreatedEvent(this.id, this.username, this.email, this.teamId)
fun UpdateUserCommand.buildEvent() = UserUpdatedEvent(this.id, this.username, this.email, this.teamId)
