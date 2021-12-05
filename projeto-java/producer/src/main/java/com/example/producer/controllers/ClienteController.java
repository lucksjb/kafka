package com.example.producer.controllers;

import java.util.Properties;

import com.example.producer.Cliente;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class ClienteController {
    
    @GetMapping
    public String publica() {
        
		Properties properties = new Properties();
		
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.ACKS_CONFIG,"all" );
        properties.put(ProducerConfig.RETRIES_CONFIG, 10 );
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://127.0.0.1:8081");
        properties.put("group.id","cliente-group-consumer");
        		
		try (Producer<String, Cliente> producer = new KafkaProducer<String, Cliente>(properties)) {
            Cliente cliente = Cliente.newBuilder()
                    .setId(20)
                    .setNome("Luciano Donizeti da silva ")
                    .setEndereco("Rua sao luiz 2086")
                    .build();

                    
            producer.send(new ProducerRecord<>("topico-teste", cliente), (rm,ex) ->{
                if (ex == null) {
                    log.info("Data sent with success!!!");
                } else {
                    log.error("Fail to send message", ex);
                }

            });
        }

        return "ok";
    }
}
