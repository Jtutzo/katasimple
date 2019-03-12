package com.jtutzo.katasimple.domaine

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.stereotype.Service
import java.util.*
import javax.inject.Inject

@Aggregate
@ProcessingGroup("user")
class User {

    @AggregateIdentifier
    var id: UUID? = null

    var username: String = ""

    var email: String = ""

    var teamId: UUID? = null

    constructor()

    @CommandHandler
    constructor(cmd: CreateUserCommand, userHelper: UserHelper) {
        userHelper.throwIfUsernameIsUsedByOtherUser(cmd.id, cmd.username)
        userHelper.throwIfEmailIsUsedByOtherUser(cmd.id, cmd.email)
        apply(cmd.buildEvent())
    }

    @CommandHandler
    fun handle(cmd: UpdateUserCommand, userHelper: UserHelper) {
        userHelper.throwIfUsernameIsUsedByOtherUser(cmd.id, cmd.username)
        userHelper.throwIfEmailIsUsedByOtherUser(cmd.id, cmd.email)
        apply(cmd.buildEvent())
    }

    @EventSourcingHandler
    fun on(evt: UserCreatedEvent) {
        this.id = evt.id
        this.username = evt.username
        this.email = evt.email
        this.teamId = evt.teamId
    }

    @EventSourcingHandler
    fun on(evt: UserUpdatedEvent) {
        this.username = evt.username
        this.email = evt.email
        this.teamId = evt.teamId
    }

}

@Service
class UserHelper @Inject constructor(private val userReadRepository: UserReadRepository) {

    fun throwIfUsernameIsUsedByOtherUser(idUser: UUID, username: String) {
        userReadRepository.findByUsername(username).ifPresent { if (it.id != idUser) throw UsernameAlreadyUsed() }
    }

    fun throwIfEmailIsUsedByOtherUser(idUser: UUID, email: String) {
        userReadRepository.findByEmail(email).ifPresent { if (it.id != idUser) throw EmailAlreadyUsed() }
    }
}

@Service
class UserEventHandler @Inject constructor(private val userWriteRepository: UserWriteRepository) {

    @EventHandler
    fun on(evt: UserCreatedEvent) {
        userWriteRepository.create(evt)
    }

    fun on(evt: UserUpdatedEvent) {
        userWriteRepository.update(evt)
    }
}
