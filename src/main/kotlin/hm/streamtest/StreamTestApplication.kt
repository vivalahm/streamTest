package hm.streamtest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StreamTestApplication

fun main(args: Array<String>) {
    runApplication<StreamTestApplication>(*args)
}