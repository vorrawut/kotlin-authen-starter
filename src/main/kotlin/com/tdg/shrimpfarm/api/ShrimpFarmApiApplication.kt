package com.tdg.shrimpfarm.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration


@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class ShrimpFarmApiApplication

fun main(args: Array<String>) {
	SpringApplication.run(ShrimpFarmApiApplication::class.java, *args)
}