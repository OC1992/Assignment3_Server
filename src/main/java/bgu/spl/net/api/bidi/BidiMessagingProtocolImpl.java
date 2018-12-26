package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import jdk.internal.net.http.common.Pair;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<String>
{
    private boolean shouldTerminate = false;
    private boolean TPC=false;
    private int myName;
    private Connections<String> connections;
    private DataSingelton dataSingelton;
    @Override
    //how should i use the getclass func? general question

    public void start(int connectionId, Connections<String> connections) {
       this.connections=connections;
       myName=connectionId;
       dataSingelton=DataSingelton.getInstance();
    }
    @Override
    public void process(String message) {

        int ind = -1;
        if (message != null)
            ind = message.indexOf(' ');
        assert message != null;
        String op = message.substring(0, ind);
        String res = message.substring(ind + 1);
        String[] splitted = res.split("\\s+");

        switch (op) {
            case "REGISTER":
                if (!dataSingelton.listOfUsers.get(myName).first.equals(splitted[0]))
                    connections.send(myName, "ERROR");
                else {
                    Pair<String, String> pair = new Pair<>(splitted[0], splitted[1]);
                    dataSingelton.listOfUsers.put(myName, pair);
                    connections.send(myName, "ACK");
                }

            case "LOGIN":

                if (!dataSingelton.listOfUsers.containsKey(myName)
                        | !dataSingelton.listOfUsers.get(myName).second.equals(splitted[1]))
                    connections.send(myName, "ERROR");
                else dataSingelton.isLogged.put(myName, splitted[0]);


        case "LOGOUT":
        if (dataSingelton.isLogged.isEmpty())
            connections.send(myName, "ERROR");

        if (dataSingelton.listOfUsers.containsKey(myName))
            dataSingelton.listOfUsers.remove(myName);

        case "FOLLOW":
        if (splitted[0].equals('0'))
            for (String s : splitted) dataSingelton.followList.get(myName).remove(s);
        else
            for (String s : splitted) dataSingelton.followList.get(myName).add(s);


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
