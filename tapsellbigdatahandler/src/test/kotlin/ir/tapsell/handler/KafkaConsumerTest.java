package ir.tapsell.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import ir.tapsell.handler.models.AdEvent;
import ir.tapsell.handler.models.ClickEvent;
import ir.tapsell.handler.models.ImpressionEvent;
import ir.tapsell.handler.models.MaliciousEvent;
import ir.tapsell.handler.repos.AdEventRepository;
import ir.tapsell.handler.repos.MaliciousEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static com.fasterxml.jackson.module.kotlin.ExtensionsKt.jacksonObjectMapper;


@SpringBootTest
@DirtiesContext
@EmbeddedKafka(topics = {"TopicTapsellImpressionEvent", "TopicTapsellClickEvent"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class KafkaConsumerTest {

    @Autowired
    public KafkaTemplate<String, String> template;

    @Autowired
    public AdEventRepository adEventRepository2;

    @Autowired
    public MaliciousEventRepository maliciousEventRepository2;

    private static final String TopicTapsellImpressionEvent = "TopicTapsellImpressionEvent";
    private static final String TopicTapsellClickEvent = "TopicTapsellClickEvent";

    @Test
    public void createAdEventTest() throws Exception {
        Faker faker = new Faker();
        String id = faker.idNumber().valid();
        long impressionTime = System.currentTimeMillis();
        ImpressionEvent impressionEvent = new ImpressionEvent(id, faker.idNumber().valid(),
                faker.company().name(), faker.number().randomDouble(10, 1, 999999),
                faker.idNumber().valid(), faker.app().name(),
                impressionTime);

        ObjectMapper JSON = jacksonObjectMapper();
        String dataString = JSON.writeValueAsString(impressionEvent);
        template.send(TopicTapsellImpressionEvent, dataString);
        Thread.sleep(1000);
        Optional<AdEvent> byId = adEventRepository2.findById(id);
        assert (byId.get().getImpressionTime() == impressionTime);
    }

    @Test
    public void goodAdClickEvent() throws Exception {
        Faker faker = new Faker();
        String id = faker.idNumber().valid();
        long impressionTime = System.currentTimeMillis();
        ImpressionEvent impressionEvent = new ImpressionEvent(id, faker.idNumber().valid(),
                faker.company().name(), faker.number().randomDouble(10, 1, 999999),
                faker.idNumber().valid(), faker.app().name(),
                impressionTime);

        ObjectMapper JSON = jacksonObjectMapper();
        String dataString = JSON.writeValueAsString(impressionEvent);
        template.send(TopicTapsellImpressionEvent, dataString);
        ClickEvent clickEvent = new ClickEvent(id, impressionTime - 100);
        dataString = JSON.writeValueAsString(clickEvent);
        Thread.sleep(1000);
        template.send(TopicTapsellClickEvent, dataString);
        Thread.sleep(1000);
        Optional<AdEvent> byId = adEventRepository2.findById(id);
        assert (byId.get().getClickTime() == impressionTime - 100);
    }


    @Test
    public void badAdClickEvent() throws Exception {
        Faker faker = new Faker();
        String id = faker.idNumber().valid();
        long impressionTime = System.currentTimeMillis();
        ImpressionEvent impressionEvent = new ImpressionEvent(id, faker.idNumber().valid(),
                faker.company().name(), faker.number().randomDouble(10, 1, 999999),
                faker.idNumber().valid(), faker.app().name(),
                impressionTime);

        ObjectMapper JSON = jacksonObjectMapper();
        String dataString = JSON.writeValueAsString(impressionEvent);
        template.send(TopicTapsellImpressionEvent, dataString);
        ClickEvent clickEvent = new ClickEvent(id, impressionTime + 100);
        dataString = JSON.writeValueAsString(clickEvent);
        Thread.sleep(1000);
        template.send(TopicTapsellClickEvent, dataString);
        Thread.sleep(1000);
        Optional<AdEvent> byId = adEventRepository2.findById(id);
        assert (byId.get().getClickTime() == 0);
        Optional<MaliciousEvent> maliciousEvent = maliciousEventRepository2.findById(id);
        assert (maliciousEvent.isPresent());
    }
}
