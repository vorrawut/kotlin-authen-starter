package com.tdg.shrimpfarm.api.dtos.response


class JwtResponse(
    var accessToken: String,
    var id: Long,
    var username: String,
    var email: String,
    val roles: List<String>
) {
    var tokenType = "Bearer"

}