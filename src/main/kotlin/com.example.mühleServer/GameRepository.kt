package com.example.mühleServer

import org.springframework.data.jpa.repository.JpaRepository

internal interface GameRepository : JpaRepository<Game?, Long?>
