package com.jtutzo.katasimple.provider

import com.jtutzo.katasimple.util.*
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

    private lateinit var userTestService: UserTestService

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
        userTestService = UserTestService(userWriteRepository)
    }

    @After
    fun reset() {
        userTestService.deleteAll()
    }

    @Test
    fun `should create user in db`() {
        // Given
        val evt = jeremyTutzo().toUserCreatedEvent()

        // When
        userWriteRepository.create(evt)

        // When
        assertThat(userReadRepository.findById(evt.id)).isEqualTo(Optional.of(jeremyTutzo().toUserProject()))
    }

    @Test
    fun `should update user in db`() {
        // Given
        userTestService.createUsers(jeremyTutzo())
        val userToUpdate = jeremyTutzo().apply {
            username = francescaCorbella().username
            email = francescaCorbella().email
        }
        val userUpdatedEvt = userToUpdate.toUserUpdatedEvent()

        // Then
        userWriteRepository.update(userUpdatedEvt)

        // When
        assertThat(userReadRepository.findById(userUpdatedEvt.id)).isEqualTo(Optional.of(userToUpdate.toUserProject()))
    }

    @Test
    fun `should find by username`() {
        // Given
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())

        // Then
        val result = userReadRepository.findByUsername(jeremyTutzo().username)

        // When
        assertThat(result).isEqualTo(Optional.of(jeremyTutzo().toUserProject()))
    }

    @Test
    fun `should find by email`() {
        // Given
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())

        // Then
        val result = userReadRepository.findByEmail(francescaCorbella().email)

        // When
        assertThat(result).isEqualTo(Optional.of(francescaCorbella().toUserProject()))
    }

    @Test
    fun `should find by id`() {
        // Given
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())

        // Then
        val result = userReadRepository.findById(francescaCorbella().id)

        // When
        assertThat(result).isEqualTo(Optional.of(francescaCorbella().toUserProject()))
    }

    @Test
    fun `should delete all users`() {
        // Given
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())

        // Then
        userWriteRepository.deleteAll()

        // When
        assertThat(userReadRepository.findAll()).isEmpty()
    }

    @Test
    fun `should find all users`() {
        // Given
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())

        // Then
        val result = userReadRepository.findAll()

        // When
        assertThat(result).containsExactly(jeremyTutzo().toUserProject(), francescaCorbella().toUserProject())
    }

}