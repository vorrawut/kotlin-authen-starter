package com.tdg.shrimpfarm.api.config


import com.tdg.shrimpfarm.api.security.AuthEntryPointJwt
import com.tdg.shrimpfarm.api.security.AuthTokenFilter
import com.tdg.shrimpfarm.api.security.JwtUtils
import com.tdg.shrimpfarm.api.service.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(var userDetailsService: UserDetailsServiceImpl,
                        private val unauthorizedHandler: AuthEntryPointJwt,
                        private val jwtUtils: JwtUtils
) : WebSecurityConfigurerAdapter() {

  @Bean
  fun authenticationJwtTokenFilter(): AuthTokenFilter {
    return AuthTokenFilter(jwtUtils, userDetailsService)
  }

  @Throws(Exception::class)
  public override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder) {
    authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder())
  }

  @Bean
  @Throws(Exception::class)
  override fun authenticationManagerBean(): AuthenticationManager {
    return super.authenticationManagerBean()
  }

  @Bean
  fun passwordEncoder(): BCryptPasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
//    http
//      .cors()
//      .and()
//      .csrf().disable()
//      .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
//      .and()
//      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//      .and()
//      .authorizeRequests()
//      .antMatchers(
//        "/api/auth/**",
//        "/error",
//        "/api/swagger-resources/**",
//        "/api/swagger/**",
//        "/api/swagger-ui/**")
//      .permitAll()
//      .anyRequest().authenticated()
//
//    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)

    http.csrf().disable()
      .authorizeRequests().antMatchers(
        "/api/auth/**",
        "/error",
        "/api/swagger-resources/**",
        "/api/swagger/**",
        "/api/swagger-ui/**")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .exceptionHandling()
      .authenticationEntryPoint(unauthorizedHandler).and().sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

    // Add a filter to validate the tokens with every request
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
  }

  @Throws(Exception::class)
  override fun configure(web: WebSecurity) {
    // TokenAuthenticationFilter will ignore the below paths
    web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**")
    web.ignoring()?.antMatchers(HttpMethod.POST, "/oauth/access-token")
    web.ignoring()?.antMatchers(HttpMethod.GET, "/api/support/guest/access-token")
    web.ignoring()?.antMatchers(
      "/",
      "/error",
      "/*.html",
      "/favicon.ico",
      "/**/*.html",
      "/**/*.css",
      "/**/*.js",
      "/ping",
      "/v2/api-docs",
      "/configuration/ui",
      "/swagger-resources/**",
      "/configuration/security",
      "/swagger-ui.html",
      "/webjars/**")
  }
}