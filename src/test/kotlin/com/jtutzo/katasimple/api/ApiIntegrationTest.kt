package com.jtutzo.katasimple.api

import com.jtutzo.katasimple.util.*
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
    private lateinit var userTestService: UserTestService

    private val url = "/users"

    @Before
    fun setup() {
        userTestService.createUsers(jeremyTutzo(), francescaCorbella())
    }

    @After
    fun reset() {
        userTestService.deleteAll()
    }

    @Test
    fun `should 200 when get all users`() {
        // When/ Then
        mockMvc.perform(get(url))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(userTestService.toUserProjectionsJson(jeremyTutzo(), francescaCorbella())))
    }

    @Test
    fun `should 200 when get user by id`() {
        // When / Then
        mockMvc.perform(get("$url/${francescaCorbella().id}"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(userTestService.toUserProjectionJson(francescaCorbella())))

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
    private lateinit var userTestService: UserTestService

    private val url = "/users"

    @After
    fun reset() {
        userTestService.deleteAll()
    }

    @Test
    fun `should 201 when create an user`() {
        // Given
        val createUserDTO = jeremyTutzo().toCreateUserDTO()

        // When / Then
        mockMvc.perform(post(url)
                .content(createUserDTO.toJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated)
    }
}
