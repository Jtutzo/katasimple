package com.jtutzo.katasimple.api

import com.jtutzo.katasimple.domaine.UserReadRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.inject.Inject

@RestController
@RequestMapping("/users")
class UserReadResource @Inject constructor(private val userReadRepository: UserReadRepository) {

    @GetMapping
    fun findAll() = ResponseEntity.ok().body(userReadRepository.findAll())

    @GetMapping("{id}")
    fun findById(@PathVariable("id") id: UUID) = userReadRepository.findById(id)
            .map { ResponseEntity.ok().body(it) }
            .orElseGet { ResponseEntity.notFound().build() }!!
}
