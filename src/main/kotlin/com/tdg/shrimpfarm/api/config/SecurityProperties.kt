package com.tdg.shrimpfarm.api.config

//import org.hibernate.validator.constraints.Length
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt-security")
class SecurityProperties {
//  @Length(min = 42, message = "Minimum length for the secret is 42.")
  var secret = "rXL1VfwAN76o2mo7auqH-Ed9p0ybgbpqm27Cr0DmypnbwSozz0I__Zi848S_t6pWB7HOBGhe8TxYw0aIsB-MtPy6PQKiy0aF0wwhp-EWvFJ9GzupIdavw5HaDJgOc9XCrjr45n5LxHwPv_osnyUxNrZJxcUV5iUTMRkCAJlxV5HC7vipka8axHsUE3Xz9TC4S8W0xnxopXzk7_17slzSI5oh_TKnsEu9i3eP0wKBbz7aIf_elCFxImDkTII9ldRYS2PQcVVNca4P68WJm8lIdwshPfEsWfvUA6X2suZdXHDqqBeGxnYzQxFDU3mZFUyjfx_YzyK7Adi8CcJxhKO6ew"
  var expirationTime: Int = 31 // in days
  var tokenPrefix = "Bearer "
  var headerString = "Authorization"
  var strength = 10
}
