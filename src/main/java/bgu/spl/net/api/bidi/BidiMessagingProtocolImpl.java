package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import jdk.internal.net.http.common.Pair;

import java.util.HashMap;
import java.util.LinkedList;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<String>
{
    private boolean shouldTerminate = false;
    private boolean TPC=false;
    private int myName;
    private Connections<String> connections;
    private DataSingelton dataSingelton=null;
    @Override
    //how should i use the getclass func? general question

    public void start(int connectionId, Connections<String> connections) {
        //what for the TPC? maybe its not needed
       if (connections.getHandler(connectionId) instanceof BlockingConnectionHandler)
           TPC=true;
       this.connections=connections;
       myName=connectionId;
        dataSingelton=DataSingelton.getInstance();
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
        int ind2 = -1;
        ind2=res.indexOf(' ');
        switch (op) {
            case "REGISTER":
                for (Pair<String, String> p:listOfUsers)
                    if (!p.first.equals(res.substring(ind, ind2)))
                        connections.send(myName,"ERROR");
                else {
                    Pair<String, String> pair=new Pair<>(res.substring(ind,ind2),res.substring(ind2));
                    listOfUsers.add(pair);
                    connections.send(myName,"ACK");
                }

            case "LOGIN":
                for (Pair<String, String> p:listOfUsers) {
                    if (!p.first.equals(res.substring(ind, ind2)) | !p.second.equals(res.substring(ind2)))
                        connections.send(myName, "ERROR");
                    else isLogged.put(p, true);
                }

            case "LOGOUT":
                Pair<String, String> pair=new Pair<String, String>(res.substring(ind ,ind2),res.substring(ind2));
                if (isLogged.isEmpty())
                    connections.send(myName,"ERROR");
                else if (listOfUsers.contains(pair))
                    isLogged.put(pair,false);

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
