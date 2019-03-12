package com.jtutzo.katasimple.domaine

import java.util.*

interface UserWriteRepository {

    fun create(evt: UserCreatedEvent)
    fun update(evt: UserUpdatedEvent)
    fun deleteAll()
}

interface UserReadRepository {

    fun findAll(): Set<UserProjection>
    fun findById(id: UUID): Optional<UserProjection>
    fun findByUsername(username: String): Optional<UserProjection>
    fun findByEmail(email: String): Optional<UserProjection>
}

