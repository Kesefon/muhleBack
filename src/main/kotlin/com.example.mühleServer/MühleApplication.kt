package com.example.mühleServer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
object mühleApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(mühleApplication::class.java, *args)
    }
}
