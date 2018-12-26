package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.io.IOException;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T>
{
    private boolean shouldTerminate = false;
    private boolean TPC=false;
    private int myName;
    Connections<T> connections;


    @Override
    //how should i use the getclass func? general question

    public void start(int connectionId, Connections<T> connections) {
        //what for the TPC? maybe its not needed
       if (connections.getHandler(connectionId) instanceof BlockingConnectionHandler)
           TPC=true;
       this.connections=connections;
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
