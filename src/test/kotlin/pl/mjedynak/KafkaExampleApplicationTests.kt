package pl.mjedynak

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils.producerProps
import org.awaitility.Awaitility.await

@SpringBootTest
@EmbeddedKafka(ports = [9092])
class KafkaExampleApplicationTests(
    @Autowired val embeddedKafkaBroker: EmbeddedKafkaBroker,
    @Autowired val adminClient: AdminClient,
    @Autowired val topicPurger: TopicPurger
) {

    private val topicName = "test.topic"

    @Test
    fun purgesTopic() {
        embeddedKafkaBroker.addTopics(NewTopic(topicName, 2, 1))
        val producer = DefaultKafkaProducerFactory<Int, String>(producerProps(embeddedKafkaBroker)).createProducer()
        val partition1 = 0
        val partition2 = 1
        producer.send(ProducerRecord(topicName, partition1, 0, "value1"))
        producer.send(ProducerRecord(topicName, partition2, 1, "value2"))

        val topicPartition1 = TopicPartition(topicName, partition1)
        val topicPartition2 = TopicPartition(topicName, partition2)
        checkOffsets(topicPartition1, topicPartition2, OffsetSpec.earliest(), 0)
        checkOffsets(topicPartition1, topicPartition2, OffsetSpec.latest(), 1)

        topicPurger.purge(topicName)

        checkOffsets(topicPartition1, topicPartition2, OffsetSpec.earliest(), 1)
        checkOffsets(topicPartition1, topicPartition2, OffsetSpec.latest(), 1)
    }

    private fun checkOffsets(
        topicPartition1: TopicPartition,
        topicPartition2: TopicPartition,
        offsetSpec: OffsetSpec,
        expectedOffset: Long
    ) {

        await().until {
            val offsets = adminClient.listOffsets(
                mapOf(
                    topicPartition1 to offsetSpec,
                    topicPartition2 to offsetSpec,
                )
            ).all().get()
            offsets[topicPartition1]?.offset() == expectedOffset
                    && offsets[topicPartition2]?.offset() == expectedOffset
        }

    }

}
