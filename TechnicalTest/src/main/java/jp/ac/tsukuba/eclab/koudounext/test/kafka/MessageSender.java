package jp.ac.tsukuba.eclab.koudounext.test.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import jp.ac.tsukuba.eclab.koudounext.proto.TestAgentOuterClass.TestAgent;
import jp.ac.tsukuba.eclab.koudounext.proto.TestConditionOuterClass.TestCondition;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

public class MessageSender {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "receive-test");
        props.put("key.serializer", ByteArraySerializer.class.getName());
        props.put("value.serializer", ByteArraySerializer.class.getName());

        KafkaProducer<byte[], byte[]> producer = new KafkaProducer<>(props);

        TestCondition sleepCondition = TestCondition.newBuilder()
                .setConditionName("sleep")
                .setAttribute("{'test':'test'}").build();

        TestCondition eatingCondition = TestCondition.newBuilder()
                .setConditionName("eating")
                .setAttribute("{'test2':'tes2t'}").build();

        TestAgent message = TestAgent.newBuilder()
                .setAgentName("Agent Test")
                .setAgentUuid(UUID.randomUUID().toString())
                .addConditions(sleepCondition).
                addConditions(eatingCondition).build();


        ProducerRecord<byte[], byte[]> record = new ProducerRecord<>("test", message.toByteArray());

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent to " + metadata.topic() + " at offset " + metadata.offset());
            } else {
                exception.printStackTrace();
            }
        });

        producer.close();
    }
}
