package jp.ac.tsukuba.eclab.koudounext.ipc.service;

public interface IIPCSenderService {
    public boolean openChannel(String address,int port);
    public boolean closeChannel();
}
