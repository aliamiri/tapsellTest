package ir.tapsell.handler

import ir.tapsell.handler.service.KafkaConsumerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HandlerApplication : CommandLineRunner {

    private val log = LoggerFactory.getLogger(HandlerApplication::class.java)
    @Autowired
    lateinit var kafkaConsumerService: KafkaConsumerService

    override fun run(vararg args: String?) {
        log.info("run")
        kafkaConsumerService.startToConsume()
    }
}

fun main(args: Array<String>) {
    runApplication<HandlerApplication>(*args)
}
