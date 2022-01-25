package com.tdg.shrimpfarm.api.dtos.request

import javax.validation.constraints.NotBlank


class LoginRequest {
    var username: @NotBlank String? = null
    var password: @NotBlank String? = null
}
