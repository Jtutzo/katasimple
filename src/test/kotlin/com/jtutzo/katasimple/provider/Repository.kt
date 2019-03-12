package com.jtutzo.katasimple.provider

import com.jtutzo.katasimple.util.*
import com.jtutzo.katasimple.util.UserTestData.Companion.USER1
import com.jtutzo.katasimple.util.UserTestData.Companion.USER2
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.jooq.impl.DSL
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.sql.DriverManager
import java.util.*

class UserWriteRepositoryTest {

    private lateinit var userWriteRepository: JooqUserWriteRepository

    private lateinit var userReadRepository: JooqUserReadRepository

    private lateinit var testService: TestService

    companion object {

        const val URL_DB = "jdbc:h2:mem:db/katasimple"

        const val USER_DB = "katasimple"

        const val PASSWORD_DB = "katasimple"

    }

    @Before
    fun setup() {
        val dsl = DSL.using(DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB))
        val flyway = Flyway.configure().dataSource(URL_DB, USER_DB, PASSWORD_DB).load()
        flyway.migrate()
        userReadRepository = JooqUserReadRepository(dsl)
        userWriteRepository = JooqUserWriteRepository(dsl)
        testService = TestService(userWriteRepository, userReadRepository)
    }

    @After
    fun reset() {
        testService.deleteAll()
    }

    @Test
    fun `should create user in db`() {
        // Given
        val evt = USER1.buildUserCreatedEvent()

        // When
        userWriteRepository.create(evt)

        // When
        assertThat(userReadRepository.findById(evt.id)).isEqualTo(Optional.of(USER1.buildUserProject()))
    }

    @Test
    fun `should update user in db`() {
        // Given
        testService.createUsers(USER1)
        val userToUpdate = USER1
                .rebuild(username = USER2.username)
                .rebuild(email = USER2.email)
        val userUpdatedEvt = userToUpdate.buildUserUpdatedEvent()

        // Then
        userWriteRepository.update(userUpdatedEvt)

        // When
        assertThat(userReadRepository.findById(userUpdatedEvt.id)).isEqualTo(Optional.of(userToUpdate.buildUserProject()))
    }

    @Test
    fun `should find by username`() {
        // Given
        testService.createUsers(USER1, USER2)

        // Then
        val result = userReadRepository.findByUsername(USER1.username)

        // When
        assertThat(result).isEqualTo(Optional.of(USER1.buildUserProject()))
    }

    @Test
    fun `should find by email`() {
        // Given
        testService.createUsers(USER1, USER2)

        // Then
        val result = userReadRepository.findByEmail(USER2.email)

        // When
        assertThat(result).isEqualTo(Optional.of(USER2.buildUserProject()))
    }

    @Test
    fun `should find by id`() {
        // Given
        testService.createUsers(USER1, USER2)

        // Then
        val result = userReadRepository.findById(USER2.id)

        // When
        assertThat(result).isEqualTo(Optional.of(USER2.buildUserProject()))
    }

    @Test
    fun `should delete all users`() {
        // Given
        testService.createUsers(USER1, USER1)

        // Then
        userWriteRepository.deleteAll()

        // When
        assertThat(userReadRepository.findAll()).isEmpty()
    }

    @Test
    fun `should find all users`() {
        // Given
        testService.createUsers(USER1, USER2)

        // Then
        val result = userReadRepository.findAll()

        // When
        assertThat(result).containsExactly(USER1.buildUserProject(), USER2.buildUserProject())
    }

}