package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.io.IOException;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T>
{
    private boolean shouldTerminate = false;
    private boolean TPC=false;
    private int myName;


    @Override
    //how should i use the getclass func?
    //its a constractor, we take the relevant handler and make a procces on it
    public void start(int connectionId, Connections<T> connections) {
       if (connections.get(connectionId) instanceof BlockingConnectionHandler)
           TPC=true;
       myName=connectionId;


    }
//message arrives and sent by the send func
    @Override
    public void process(T message) {

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
