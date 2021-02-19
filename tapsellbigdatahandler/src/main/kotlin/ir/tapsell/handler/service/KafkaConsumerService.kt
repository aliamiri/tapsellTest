package ir.tapsell.handler.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ir.tapsell.handler.repos.AdEventRepository
import ir.tapsell.handler.HandlerApplication
import ir.tapsell.handler.repos.MaliciousEventRepository
import ir.tapsell.handler.models.AdEvent
import ir.tapsell.handler.models.ClickEvent
import ir.tapsell.handler.models.ImpressionEvent
import ir.tapsell.handler.models.MaliciousEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*


@Service
class KafkaConsumerService {
    @Autowired
    lateinit var adEventRepository: AdEventRepository

    @Autowired
    lateinit var maliciousEventRepository: MaliciousEventRepository

    @Value("\${bootstrap.servers}")
    lateinit var bootstrapServers: String

    val JSON = jacksonObjectMapper()

    private val log = LoggerFactory.getLogger(HandlerApplication::class.java)

    /*
     * This function will create a consumer properties
     */
    private fun createClickConsumer(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["group.id"] = "tapsell-click-consumer-group"
        props["application.id"] = "tapsell-click-consumer-application"
        props["auto.offset.reset"] = "earliest"
        return props
    }

    private fun createImpConsumer(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["group.id"] = "tapsell-Imp-consumer-group"
        props["application.id"] = "tapsell-Imp-consumer-application"
        props["auto.offset.reset"] = "earliest"
        return props
    }

    /*
     * Our journey begins here
     */
    fun startToConsume() {
        log.info("Start consuming")
        impressionConsumer()
        clickConsumer()
    }


    /*
     * Stream for click event will be build here
     */
    private fun clickConsumer() {
        val streamsBuilder = StreamsBuilder()
        val jsonStream: KStream<String, String> = streamsBuilder
                .stream<String, String>("TopicTapsellClickEvent", Consumed.with(Serdes.String(), Serdes.String()))

        val stream: KStream<String, ClickEvent> = jsonStream.mapValues { v ->
            JSON.readValue(v, ClickEvent::class.java)
        }

        stream.foreach { _, u ->
            clickHandler(u)
        }

        val topology = streamsBuilder.build()

        val props = createClickConsumer()
        val streams = KafkaStreams(topology, props)
        streams.start()
    }


    /*
     * Stream for Impression event will be build here
     */
    private fun impressionConsumer() {
        val streamsBuilder = StreamsBuilder()
        val jsonStream: KStream<String, String> = streamsBuilder
                .stream<String, String>("TopicTapsellImpressionEvent",
                        Consumed.with(Serdes.String(), Serdes.String()))

        val stream: KStream<String, ImpressionEvent> = jsonStream.mapValues { v ->
            JSON.readValue(v, ImpressionEvent::class.java)
        }

        stream.foreach { _, u ->
            impressionHandler(u)
        }

        val impTopology = streamsBuilder.build()

        val props = createImpConsumer()
        val streams = KafkaStreams(impTopology, props)
        streams.start()
    }

    /*
     * Process click events
     */
    private fun clickHandler(u: ClickEvent) {
        val optionalEvent = adEventRepository.findById(u.requestId)
        //Check if event exist in db(it should!)
        if (optionalEvent.isPresent) {
            val adEvent = optionalEvent.get()
            //Check if the event is valid or fake click
            if (u.clickTime > adEvent.impressionTime) {
                log.error("Suspicious behavior from ${adEvent.appTitle} ad should be terminated")
                val optionalMaliciousEvent = maliciousEventRepository.findById(u.requestId)
                //Save the suspicious behavior for when we want to take action! (Every fake click we be added)
                if (optionalMaliciousEvent.isPresent) {
                    val maliciousEvent = optionalMaliciousEvent.get()
                    maliciousEvent.count += 1
                    maliciousEventRepository.save(maliciousEvent)
                } else {
                    val maliciousEvent = MaliciousEvent(adEvent.requestId, adEvent.adId,
                            adEvent.adTitle, adEvent.advertiserCost,
                            adEvent.appId, adEvent.appTitle,
                            adEvent.impressionTime, 1)
                    maliciousEventRepository.save(maliciousEvent)
                }
            } else {
                // last click time would be saved for the adEvent
                adEvent.clickTime = u.clickTime
                adEventRepository.save(adEvent)
            }
        } else
            log.error("Event ${u.requestId} dosn't exist")
    }

    /*
     * Process impression events
     */
    private fun impressionHandler(impressionEvent: ImpressionEvent) {
        var adEvent = AdEvent(impressionEvent.requestId, impressionEvent.adId,
                impressionEvent.adTitle, impressionEvent.advertiserCost,
                impressionEvent.appId, impressionEvent.appTitle,
                impressionEvent.impressionTime, 0)
        try {
            adEventRepository.insert(adEvent)
        } catch (e: Exception) {
            log.error(e.message)
        }
    }
}