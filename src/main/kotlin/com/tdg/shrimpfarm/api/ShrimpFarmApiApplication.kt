package com.tdg.shrimpfarm.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@SpringBootApplication
@EnableJpaRepositories
class ShrimpFarmApiApplication

fun main(args: Array<String>) {
	SpringApplication.run(ShrimpFarmApiApplication::class.java, *args)
}