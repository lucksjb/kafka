package com.example.consumer.controllers;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import com.example.producer.Cliente;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class ClienteController {
    private String topico = "topico-teste";

    @GetMapping
    public String consome() {
        
        

		Properties props = new Properties();
		
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put("client.id","my-app-java-consumer");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "cliente-group-consumer-999");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		// props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put("auto.offset.reset", "earliest");
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
		
		props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://127.0.0.1:8081");
		props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
		
        try (Consumer<String, Cliente> consumer = new KafkaConsumer<String, Cliente> (props)) {
            consumer.subscribe(Arrays.asList(this.topico));
            // TopicPartition part = new TopicPartition(this.topico, 0);
            // consumer.seek(part, 0);
            try {
                ConsumerRecords<String, Cliente> records = consumer.poll(Duration.ofSeconds(1));
                for(ConsumerRecord<String, Cliente> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                }
                
                // consumer.poll(Duration.ofMillis(1000)).forEach(record -> {

                //     log.error("Recebendo cliente "+record.value());
                 

                //    // Cliente cliente = record.value();

                //     log.error("pocessando cliente ");
                // });

                consumer.commitSync();

            } catch (Exception ex) {
                log.error("Erro ao processar mensagem", ex);
            }
        }
	
        return "ok";
    }
}
