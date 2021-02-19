package ir.tapsell.generator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.javafaker.Faker
import ir.tapsell.demogenerator.models.ClickEvent
import ir.tapsell.generator.models.ImpressionEvent
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@RestController
class KafkaController() {

    @Value("\${bootstrap.servers}")
    lateinit var bootstrapServers: String

    private fun createProducer(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    val JSON = jacksonObjectMapper()

    @GetMapping("/sendAd")
    fun sendAd(): ResponseEntity<String> {
        val faker = Faker()
        faker.elderScrolls()
        val producer = createProducer()
        val iterCount = Random().nextInt(1000)
        var counter = 0
        for (i in 0..iterCount) {
            //Every thing is generated randomly
            //Except impression time which is a random time between now and 100 seconds later
            val impressionEvent = ImpressionEvent(faker.idNumber().valid(), faker.idNumber().valid(),
                    faker.company().name(), faker.number().randomDouble(10, 1, 999999),
                    faker.idNumber().valid(), faker.app().name(),
                    System.currentTimeMillis() + Random().nextInt(100000))

            val existing = requestIds.get(impressionEvent.requestId)
            //Check if impression already exists
            if (existing == null) {
                requestIds.put(impressionEvent.requestId, impressionEvent.impressionTime)
                counter++
                val dataString = JSON.writeValueAsString(impressionEvent)
                val future = producer.send(ProducerRecord("TopicTapsellImpressionEvent", i.toString(), dataString))
                future.get()
            }
        }
        return ResponseEntity.ok(" sent $counter events to TopicTapsellImpressionEvent")
    }

    val requestIds: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    @GetMapping("/sendGoodClick")
    fun sendGoodClick(): ResponseEntity<String> {
        val faker = Faker()
        faker.elderScrolls()
        val producer = createProducer()
        val keys = requestIds.keys
        var counter = 0
        keys.iterator().forEachRemaining {
            val b = Random().nextInt(100)
            val currentTimeMillis = System.currentTimeMillis()
            val impressionTime = requestIds.getValue(it)
            //Here I assumed that a viewer would click on a banner with probability 15%
            //Also since we want to generate good clicks we compare current time and impression time
            if (b < 15 && currentTimeMillis < impressionTime) {
                val clickTime = faker.number().randomNumber(2, true)
                val adEvent = ClickEvent(it, clickTime)
                val dataString = JSON.writeValueAsString(adEvent)
                val future = producer.send(ProducerRecord("TopicTapsellClickEvent", impressionTime.toString(), dataString))
                future.get()
                counter++
            }
        }
        return ResponseEntity.ok(" sent $counter good events to TopicTapsellClickEvent")
    }


    @GetMapping("/sendBadClick")
    fun sendBadClick(): ResponseEntity<String> {
        val faker = Faker()
        faker.elderScrolls()
        val producer = createProducer()
        val keys = requestIds.keys
        var counter = 0
        keys.iterator().forEachRemaining {
            val b = Random().nextInt(100)
            val currentTimeMillis = System.currentTimeMillis()
            val impressionTime = requestIds.getValue(it)
            //Here I assumed that we would have an ill behaved  advertiser with probability 10%
            //Also since we want to generate bad clicks we compare current time and impression time
            if (b < 10 && currentTimeMillis > impressionTime) {
                val clickTime = faker.number().randomNumber(2, true)
                val adEvent = ClickEvent(it, clickTime)
                val dataString = JSON.writeValueAsString(adEvent)
                val future = producer.send(ProducerRecord("TopicTapsellClickEvent", impressionTime.toString(), dataString))
                future.get()
                counter++
            }
        }
        return ResponseEntity.ok(" sent $counter good events to TopicTapsellClickEvent")
    }

}