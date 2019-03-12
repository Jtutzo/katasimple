package com.jtutzo.katasimple.domaine

import com.jtutzo.katasimple.util.*
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
    fun setup() {
        MockitoAnnotations.initMocks(this)
        fixture = AggregateTestFixture<User>(User::class.java)
        fixture.registerInjectableResource(helperUserHelper)
    }

    @Test
    fun `should create user`() {
        // Given
        val createUserCmd = jeremyTutzo().toCreateUserCmd()

        // When
        val result = fixture.givenNoPriorActivity().`when`(createUserCmd)

        // Then
        result.expectSuccessfulHandlerExecution()
        result.expectEvents(createUserCmd.buildEvent())
        result.expectState {
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
        val createUserCmd = jeremyTutzo().toCreateUserCmd()
        `when`(helperUserHelper.throwIfUsernameIsUsedByOtherUser(createUserCmd.id, createUserCmd.username)).thenThrow(UsernameAlreadyUsed())

        // When
        val result = fixture.givenNoPriorActivity().`when`(createUserCmd)

        // Then
        result.expectException(UsernameAlreadyUsed::class.java)
        result.expectExceptionMessage("Username is already used.")
    }

    @Test
    fun `shouldn't create user when email is already used by other user`() {
        // Given
        val createUserCmd = jeremyTutzo().toCreateUserCmd()
        `when`(helperUserHelper.throwIfEmailIsUsedByOtherUser(createUserCmd.id, createUserCmd.email)).thenThrow(EmailAlreadyUsed())

        // When
        val result = fixture.givenNoPriorActivity().`when`(createUserCmd)

        // Then
        result.expectException(EmailAlreadyUsed::class.java)
        result.expectExceptionMessage("Email is already used.")
    }

    @Test
    fun `should update user`() {
        // Given
        val createUserCmd = jeremyTutzo().toCreateUserCmd()
        val updateUserCmd = jeremyTutzo().apply {
            username = "jeremy.bg"
            email = "jbg@email.com"
            teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93")
        }.toUpdateUserCmd()

        // When
        val result = fixture.given(createUserCmd.buildEvent()).`when`(updateUserCmd)

        // Then
        result.expectSuccessfulHandlerExecution()
        result.expectEvents(updateUserCmd.buildEvent())
        result.expectState {
            it.isEqualsTo(
                    username = updateUserCmd.username,
                    email = updateUserCmd.email,
                    teamId = updateUserCmd.teamId)
        }
    }

    @Test
    fun `shouldn't update user when username is already used by other user`() {
        // Given
        val createUserCmd = jeremyTutzo().toCreateUserCmd()
        val updateUserCmd = jeremyTutzo().apply {
            username = "jeremy.bg"
            email = "jbg@email.com"
            teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93")
        }.toUpdateUserCmd()
        `when`(helperUserHelper.throwIfUsernameIsUsedByOtherUser(updateUserCmd.id, updateUserCmd.username)).thenThrow(UsernameAlreadyUsed())

        // When
        val result = fixture.given(createUserCmd.buildEvent()).`when`(updateUserCmd)

        // Then
        result.expectException(UsernameAlreadyUsed::class.java)
        result.expectExceptionMessage("Username is already used.")
    }

    @Test
    fun `shouldn't update user when email is already used by other user`() {
        // Given
        val createUserCmd = jeremyTutzo().toCreateUserCmd()
        val updateUserCmd = jeremyTutzo().apply {
            username = "jeremy.bg"
            email = "jbg@email.com"
            teamId = UUID.fromString("814442e2-3d15-11e9-b210-d663bd873d93")
        }.toUpdateUserCmd()
        `when`(helperUserHelper.throwIfEmailIsUsedByOtherUser(updateUserCmd.id, updateUserCmd.email)).thenThrow(EmailAlreadyUsed())

        // When
        val result = fixture.given(createUserCmd.buildEvent()).`when`(updateUserCmd)

        // Then
        result.expectException(EmailAlreadyUsed::class.java)
        result.expectExceptionMessage("Email is already used.")
    }

}

@RunWith(MockitoJUnitRunner::class)
class UserHelperTest {

    @Mock
    private lateinit var userReadRepository: UserReadRepository

    private lateinit var userHelper: UserHelper

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        userHelper = UserHelper(userReadRepository)
    }

    @Test
    fun `should throw UsernameAlreadyUsed exception when the username is used for other user`() {
        // Given
        `when`(userReadRepository.findByUsername(jeremyTutzo().username)).thenReturn(Optional.ofNullable(francescaCorbella()
                .apply { username = jeremyTutzo().username }
                .toUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfUsernameIsUsedByOtherUser(jeremyTutzo().id, jeremyTutzo().username)
        }

        // Then
        assertThat(throwable).isInstanceOf(UsernameAlreadyUsed::class.java)
        assertThat(throwable).hasMessage("Username is already used.")
    }

    @Test
    fun `shouldn't throw UsernameAlreadyUsed exception when the username is used by this`() {
        // Given
        `when`(userReadRepository.findByUsername(jeremyTutzo().username)).thenReturn(Optional.ofNullable(jeremyTutzo().toUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfUsernameIsUsedByOtherUser(jeremyTutzo().id, jeremyTutzo().username)
        }

        // Then
        assertThat(throwable).isNull()
    }

    @Test
    fun `should throw EmailAlreadyUsed exception when the email is used for other user`() {
        // Given
        `when`(userReadRepository.findByEmail(jeremyTutzo().email)).thenReturn(Optional.ofNullable(francescaCorbella()
                .apply { email = jeremyTutzo().email }
                .toUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfEmailIsUsedByOtherUser(jeremyTutzo().id, jeremyTutzo().email)
        }

        // Then
        assertThat(throwable).isInstanceOf(EmailAlreadyUsed::class.java)
        assertThat(throwable).hasMessage("Email is already used.")
    }

    @Test
    fun `shouldn't throw EmailAlreadyUsed exception when the email is used by this`() {
        // Given
        `when`(userReadRepository.findByEmail(jeremyTutzo().email)).thenReturn(Optional.ofNullable(jeremyTutzo().toUserProject()))

        // When
        val throwable = catchThrowable {
            userHelper.throwIfEmailIsUsedByOtherUser(jeremyTutzo().id, jeremyTutzo().email)
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
    fun setup() {
        MockitoAnnotations.initMocks(this)
        userEventHandler = UserEventHandler(userWriteRepository)
    }

    @Test
    fun `should persist user created when UserCreatedEvent is call`() {
        // Given
        val event = jeremyTutzo().toCreateUserCmd().buildEvent()

        // When
        userEventHandler.on(event)

        // Then
        verify(userWriteRepository, only()).create(event)
    }

    @Test
    fun `should persist user updated when UserUpdatedEvent is call`() {
        // Given
        val event = jeremyTutzo().toUpdateUserCmd().buildEvent()

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
