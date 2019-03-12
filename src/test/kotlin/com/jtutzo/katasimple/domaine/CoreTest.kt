package com.jtutzo.katasimple.domaine

import com.jtutzo.katasimple.util.UserTestData.Companion.USER1
import com.jtutzo.katasimple.util.UserTestData.Companion.USER2
import com.jtutzo.katasimple.util.buildCreateUserCmd
import com.jtutzo.katasimple.util.buildUpdateUserCmd
import com.jtutzo.katasimple.util.buildUserProject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class UserTest {

    private lateinit var fixture: FixtureConfiguration<User>

    @Mock
    private lateinit var helperUserHelper: UserHelper

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        fixture = AggregateTestFixture<User>(User::class.java)
        fixture.registerInjectableResource(helperUserHelper)
    }

    @Test
    fun `should create user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        doNothing().`when`(helperUserHelper).throwIfUsernameIsUsedByOtherUser(createUserCmd.id, createUserCmd.username)

        // Then / When
        fixture.givenNoPriorActivity()
                .`when`(createUserCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(createUserCmd.buildEvent())
                .expectState {
                    it.isEqualsTo(
                            id = createUserCmd.id,
                            username = createUserCmd.username,
                            email = createUserCmd.email,
                            teamId = createUserCmd.teamId)
                }
    }

    @Test
    fun `shouldn't create user when username is already used by other user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        `when`(helperUserHelper.throwIfUsernameIsUsedByOtherUser(createUserCmd.id, createUserCmd.username)).thenThrow(UsernameAlreadyUsed())

        // Then / When
        fixture.givenNoPriorActivity()
                .`when`(createUserCmd)
                .expectException(UsernameAlreadyUsed::class.java)
                .expectExceptionMessage("Username is already used.")
    }

    @Test
    fun `shouldn't create user when email is already used by other user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        `when`(helperUserHelper.throwIfEmailIsUsedByOtherUser(createUserCmd.id, createUserCmd.email)).thenThrow(EmailAlreadyUsed())

        // Then / When
        fixture.givenNoPriorActivity()
                .`when`(createUserCmd)
                .expectException(EmailAlreadyUsed::class.java)
                .expectExceptionMessage("Email is already used.")
    }

    @Test
    fun `should update user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        val updateUserCmd = USER1
                .rebuild(username = "jeremy.bg")
                .rebuild(email = "jbg@email.com")
                .rebuild(teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93"))
                .buildUpdateUserCmd()
        doNothing().`when`(helperUserHelper).throwIfUsernameIsUsedByOtherUser(createUserCmd.id, createUserCmd.username)

        // Then / When
        fixture.given(createUserCmd.buildEvent())
                .`when`(updateUserCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(updateUserCmd.buildEvent())
                .expectState {
                    it.isEqualsTo(
                            username = updateUserCmd.username,
                            email = updateUserCmd.email,
                            teamId = updateUserCmd.teamId)
                }
    }

    @Test
    fun `shouldn't update user when username is already used by other user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        val updateUserCmd = USER1
                .rebuild(username = "jeremy.bg")
                .rebuild(email = "jbg@email.com")
                .rebuild(teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93"))
                .buildUpdateUserCmd()
        `when`(helperUserHelper.throwIfUsernameIsUsedByOtherUser(updateUserCmd.id, updateUserCmd.username)).thenThrow(UsernameAlreadyUsed())

        // Then / When
        fixture.given(createUserCmd.buildEvent())
                .`when`(updateUserCmd)
                .expectException(UsernameAlreadyUsed::class.java)
                .expectExceptionMessage("Username is already used.")
    }

    @Test
    fun `shouldn't update user when email is already used by other user`() {
        // Given
        val createUserCmd = USER1.buildCreateUserCmd()
        val updateUserCmd = USER1
                .rebuild(username = "jeremy.bg")
                .rebuild(email = "jbg@email.com")
                .rebuild(teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93"))
                .buildUpdateUserCmd()
        `when`(helperUserHelper.throwIfEmailIsUsedByOtherUser(updateUserCmd.id, updateUserCmd.email)).thenThrow(EmailAlreadyUsed())

        // Then / When
        fixture.given(createUserCmd.buildEvent())
                .`when`(updateUserCmd)
                .expectException(EmailAlreadyUsed::class.java)
                .expectExceptionMessage("Email is already used.")
    }

}

@RunWith(MockitoJUnitRunner::class)
class UserHelperTest {

    @Mock
    private lateinit var userReadRepository: UserReadRepository

    private lateinit var userHelper: UserHelper

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        userHelper = UserHelper(userReadRepository)
    }

    @Test
    fun `should throw UsernameAlreadyUsed exception when the username is used for other user`() {
        // Given
        `when`(userReadRepository.findByUsername(USER1.username)).thenReturn(Optional.ofNullable(USER2
                .rebuild(username = USER1.username)
                .buildUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfUsernameIsUsedByOtherUser(USER1.id, USER1.username)
        }

        // Then
        assertThat(throwable).isInstanceOf(UsernameAlreadyUsed::class.java)
        assertThat(throwable).hasMessage("Username is already used.")
    }

    @Test
    fun `shouldn't throw UsernameAlreadyUsed exception when the username is used by this`() {
        // Given
        `when`(userReadRepository.findByUsername(USER1.username)).thenReturn(Optional.ofNullable(USER1.buildUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfUsernameIsUsedByOtherUser(USER1.id, USER1.username)
        }

        // Then
        assertThat(throwable).isNull()
    }

    @Test
    fun `should throw EmailAlreadyUsed exception when the email is used for other user`() {
        // Given
        `when`(userReadRepository.findByEmail(USER1.email)).thenReturn(Optional.ofNullable(USER2
                .rebuild(email = USER1.email)
                .buildUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfEmailIsUsedByOtherUser(USER1.id, USER1.email)
        }

        // Then
        assertThat(throwable).isInstanceOf(EmailAlreadyUsed::class.java)
        assertThat(throwable).hasMessage("Email is already used.")
    }

    @Test
    fun `shouldn't throw EmailAlreadyUsed exception when the email is used by this`() {
        // Given
        `when`(userReadRepository.findByEmail(USER1.email)).thenReturn(Optional.ofNullable(USER1.buildUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfEmailIsUsedByOtherUser(USER1.id, USER1.email)
        }

        // Then
        assertThat(throwable).isNull()
    }
}

@RunWith(MockitoJUnitRunner::class)
class UserEventHandlerTest {

    private lateinit var userEventHandler: UserEventHandler

    @Mock
    private lateinit var userWriteRepository: UserWriteRepository

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        userEventHandler = UserEventHandler(userWriteRepository)
    }

    @Test
    fun `should persist user created when UserCreatedEvent is call`() {
        // Given
        val event = USER1.buildCreateUserCmd().buildEvent()

        // When
        userEventHandler.on(event)

        // Then
        verify(userWriteRepository, only()).create(event)
    }

    @Test
    fun `should persist user updated when UserUpdatedEvent is call`() {
        // Given
        val event = USER1.buildUpdateUserCmd().buildEvent()

        // When
        userEventHandler.on(event)

        // Then
        verify(userWriteRepository, only()).update(event)
    }

}

fun User.isEqualsTo(id: UUID? = this.id,
                    username: String = this.username,
                    email: String = this.email,
                    teamId: UUID? = this.teamId) {
    assertThat(this.id).isEqualTo(id)
    assertThat(this.username).isEqualTo(username)
    assertThat(this.email).isEqualTo(email)
    assertThat(this.teamId).isEqualTo(teamId)

}
