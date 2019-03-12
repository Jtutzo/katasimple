package com.jtutzo.katasimple.util

import com.jtutzo.katasimple.domaine.UserReadRepository
import com.jtutzo.katasimple.domaine.UserWriteRepository
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class TestService @Inject constructor(private val userWriteRepository: UserWriteRepository, private val userReadRepository: UserReadRepository) {

    fun createUsers(vararg users: UserTestData) {
        users.map { userWriteRepository.create(it.buildUserCreatedEvent()) }
    }

    fun deleteAll() {
        userWriteRepository.deleteAll()
    }

    fun toUserProjectionJson(user: UserTestData): String = user.buildUserProject().buildToJson()

    fun toUserProjectionsJson(vararg users: UserTestData): String = users
            .map { it.buildUserProject() }
            .toSet()
            .buildToJson()
}