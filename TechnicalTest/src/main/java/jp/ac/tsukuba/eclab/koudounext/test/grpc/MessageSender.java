package jp.ac.tsukuba.eclab.koudounext.test.grpc;


import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import jp.ac.tsukuba.eclab.koudounext.proto.objects.TestConditionOuterClass.TestCondition;
import jp.ac.tsukuba.eclab.koudounext.proto.objects.TestAgentOuterClass.TestAgent;
import jp.ac.tsukuba.eclab.koudounext.proto.service.*;

import java.io.IOException;
import java.util.UUID;

public class MessageSender {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 2983)
                .usePlaintext()
                .build();

        TestCondition sleepCondition = TestCondition.newBuilder()
                .setConditionName("sleep")
                .setAttribute("{'test':'test'}").build();

        TestCondition eatingCondition = TestCondition.newBuilder()
                .setConditionName("eating")
                .setAttribute("{'test2':'tes2t'}").build();

        TestAgent testAgent = TestAgent.newBuilder()
                .setAgentName("Agent Test")
                .setAgentUuid(UUID.randomUUID().toString())
                .addConditions(sleepCondition).
                addConditions(eatingCondition).build();
        ByteString payload = testAgent.toByteString();

        IPCRequest request = IPCRequest.newBuilder()
                .setType(IPCSerializationType.JAVA_SERIALIZED)
                .setPayload(payload)
                .build();

        IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
        IPCResponse response = stub.sendIPC(request);
        System.out.println("Status: " + response.getStatus());
    }
}
