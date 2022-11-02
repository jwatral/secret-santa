package dev.jwatral.secretsanta

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecretSantaApplication

fun main(args: Array<String>) {
    runApplication<SecretSantaApplication>(*args)
}
