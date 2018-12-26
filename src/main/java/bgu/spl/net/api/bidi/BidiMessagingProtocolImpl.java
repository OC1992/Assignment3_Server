package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;

import java.util.LinkedList;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<String>
{
    private boolean shouldTerminate = false;
    private boolean TPC=false;
    private int myName;
    private Connections<String> connections;
    private LinkedList<String> listOfUsers=new LinkedList<String>();


    @Override
    //how should i use the getclass func? general question

    public void start(int connectionId, Connections<String> connections) {
        //what for the TPC? maybe its not needed
       if (connections.getHandler(connectionId) instanceof BlockingConnectionHandler)
           TPC=true;
       this.connections=connections;
       myName=connectionId;


    }
//message arrives and sent by the send func
    @Override
    public void process(String message) {
        int ind = -1;
        if (message != null)
            ind = message.indexOf(' ');
        assert message != null;
        String op = message.substring(0, ind);
        String res=message.substring(ind);
        switch (op) {
            case "REGISTER":
                int ind2 = -1;
                ind2=res.indexOf(' ');
                if (!listOfUsers.contains(res.substring(ind,ind2)))
                    connections.send(myName,"ERROR");
                else listOfUsers.add(res.substring(ind,ind2));

            case "LOGIN":

            case "LOGOUT":

            case "FOLLOW":

            case "POST":

            case "PM":

            case "USERLIST":

            case "STAT":

            case "NOTIFICATION":

            case "ACK":

            case "ERROR":



        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

}
