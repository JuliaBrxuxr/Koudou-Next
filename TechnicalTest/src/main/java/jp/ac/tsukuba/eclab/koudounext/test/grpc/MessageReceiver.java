package jp.ac.tsukuba.eclab.koudounext.test.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import jp.ac.tsukuba.eclab.koudounext.proto.service.IPCRequest;
import jp.ac.tsukuba.eclab.koudounext.proto.service.IPCResponse;
import jp.ac.tsukuba.eclab.koudounext.proto.service.IPCResponseStatus;
import jp.ac.tsukuba.eclab.koudounext.proto.service.IPCServiceGrpc;

import java.io.IOException;

public class MessageReceiver {
    public static void main(String[] args) {
        Server server = ServerBuilder
                .forPort(2983)
                .addService(new IPCServiceImpl())
                .build();
        try {
            server.start();
            System.out.println("Server started");
            server.awaitTermination();
        } catch (IOException e) {
            System.out.println("Failed to start server");
        } catch (InterruptedException e) {
            System.out.println("Termination interrupted");
        }

    }

    static class IPCServiceImpl extends IPCServiceGrpc.IPCServiceImplBase {
        @Override
        public void sendIPC(IPCRequest request, StreamObserver<IPCResponse> responseObserver) {
            System.out.println("Received request: " + request);
            IPCResponse response = IPCResponse.newBuilder()
                    .setStatus(IPCResponseStatus.SUCCESS)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
