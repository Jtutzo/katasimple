package com.jtutzo.katasimple.api

import com.jtutzo.katasimple.domaine.CreateUserCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.inject.Inject

@RestController
@RequestMapping("/users")
class UserWriteResource @Inject constructor(private val commandGateway: CommandGateway) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody dto: CreateUserDTO) {
        commandGateway.sendAndWait<CreateUserCommand>(CreateUserCommand(UUID.randomUUID(), dto.username, dto.email, dto.teamId))
    }
}