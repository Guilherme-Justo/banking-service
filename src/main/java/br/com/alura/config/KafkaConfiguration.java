package br.com.alura.config;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.Config;

import java.util.Properties;

@ApplicationScoped
public class KafkaConfiguration {

    private final KafkaProducer<String, String> producer;

    public KafkaConfiguration(Config config) {
        String kafkaHost = config.getValue("producer.kafka.host", String.class);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                Log.info("Mensagem enviada: " + metadata.offset());
            } else {
                Log.error("Erro ao enviar mensagem: " + exception.getMessage());
            }
        });
    }
}
