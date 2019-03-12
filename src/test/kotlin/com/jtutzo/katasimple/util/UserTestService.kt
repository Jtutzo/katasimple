package com.jtutzo.katasimple.util

import com.jtutzo.katasimple.domaine.UserWriteRepository
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class UserTestService @Inject constructor(private val userWriteRepository: UserWriteRepository) {

    fun createUsers(vararg users: UserTestEntity) {
        users.map { userWriteRepository.create(it.toUserCreatedEvent()) }
    }

    fun deleteAll() {
        userWriteRepository.deleteAll()
    }

    fun toUserProjectionJson(user: UserTestEntity): String = user.toUserProject().toJson()

    fun toUserProjectionsJson(vararg users: UserTestEntity): String = users
            .map { it.toUserProject() }
            .toSet()
            .toJson()
}