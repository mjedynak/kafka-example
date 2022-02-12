package pl.mjedynak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaExampleApplication

fun main(args: Array<String>) {
	val context = runApplication<KafkaExampleApplication>(*args)
}
