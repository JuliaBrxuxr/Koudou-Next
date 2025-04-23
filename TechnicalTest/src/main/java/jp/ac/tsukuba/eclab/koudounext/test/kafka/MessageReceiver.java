package jp.ac.tsukuba.eclab.koudounext.test.kafka;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import jp.ac.tsukuba.eclab.koudounext.proto.TestAgentOuterClass.TestAgent;
import jp.ac.tsukuba.eclab.koudounext.proto.TestConditionOuterClass.TestCondition;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MessageReceiver {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "receive-test");
        props.put("key.deserializer", ByteArrayDeserializer.class.getName());
        props.put("value.deserializer", ByteArrayDeserializer.class.getName());
        props.put("auto.offset.reset", "latest");
        props.put("max.poll.records", "1000");
        props.put("fetch.min.bytes", "1");
        props.put("fetch.max.wait.ms", "100");

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test"));

        System.out.println("Waiting for messages...");

        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<byte[], byte[]> record : records) {
                try {
                    TestAgent message = TestAgent.parseFrom(record.value());
                    System.out.println("Received message: " + message.getAgentName() +
                            message.getConditionsList());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
