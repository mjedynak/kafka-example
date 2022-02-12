package pl.mjedynak

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.RecordsToDelete
import org.apache.kafka.common.TopicPartition
import org.springframework.stereotype.Component

@Component
class TopicPurger(private val adminClient: AdminClient) {

    fun purge(topicName: String) {
        val topicDescription = adminClient.describeTopics(listOf(topicName)).all().get()[topicName]
        val partitionSize = topicDescription?.partitions()?.size
        val recordsToDelete = (0..partitionSize!!).associate { partitionIndex ->
            TopicPartition(topicName, partitionIndex) to RecordsToDelete.beforeOffset(-1)
        }
        adminClient.deleteRecords(recordsToDelete)
    }

}