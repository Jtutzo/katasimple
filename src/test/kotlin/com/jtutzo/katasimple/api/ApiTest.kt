package com.jtutzo.katasimple.api

import com.jtutzo.katasimple.util.TestService
import com.jtutzo.katasimple.util.UserTestData.Companion.USER1
import com.jtutzo.katasimple.util.UserTestData.Companion.USER2
import com.jtutzo.katasimple.util.buildCreateUserDTO
import com.jtutzo.katasimple.util.buildToJson
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserReadResourceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testService: TestService

    private val url = "/users"

    @Before
    fun setup() {
        testService.createUsers(USER1, USER2)
    }

    @After
    fun reset() {
        testService.deleteAll()
    }

    @Test
    fun `should 200 when get all users`() {
        // When/ Then
        mockMvc.perform(get(url))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(testService.toUserProjectionsJson(USER1, USER2)))
    }

    @Test
    fun `should 200 when get user by id`() {
        // When / Then
        mockMvc.perform(get("$url/${USER2.id}"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(testService.toUserProjectionJson(USER2)))

    }

    @Test
    fun `should 404 when no user match with id`() {
        // When / Then
        mockMvc.perform(get("$url/79529ca6-3dc8-11e9-b210-d663bd873d93"))
                .andExpect(status().isNotFound)

    }

}

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserWriteResourceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testService: TestService

    private val url = "/users"

    @After
    fun reset() {
        testService.deleteAll()
    }

    @Test
    fun `should 201 when create an user`() {
        // Given
        val createUserDTO = USER1.buildCreateUserDTO()

        // When / Then
        mockMvc.perform(post(url)
                .content(createUserDTO.buildToJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated)
    }
}
