package com.jtutzo.katasimple.api

import java.util.*

data class CreateUserDTO(val username: String, val email: String, val teamId: UUID? = null)