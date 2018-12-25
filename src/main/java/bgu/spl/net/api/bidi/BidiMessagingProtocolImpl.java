package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T>
{
    private boolean shouldTerminate = false;
    ConnectionHandler<T> handler=new BlockingConnectionHandler<>();

    @Override
    public void start(int connectionId, Connections<T> connections) {
        handler

    }

    @Override
    public void process(T message) {

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
