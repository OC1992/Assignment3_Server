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
        String res=message.substring(ind+1);

        switch (op) {
            case "REGISTER":
                String[]splitted=res.split("\\s+");

                for (Pair<String, String> p:dataSingelton.listOfUsers)
                    if (!p.first.equals((splitted[0])))
                        connections.send(myName,"ERROR");
                else {
                    Pair<String, String> pair=new Pair<>(splitted[0],splitted[1]);
                        dataSingelton.listOfUsers.add(pair);
                    connections.send(myName,"ACK");
                }

            case "LOGIN":
                for (Pair<String, String> p:dataSingelton.listOfUsers) {
                    if (!p.first.equals(splitted[0]) | !p.second.equals(splitted[1]))
                        connections.send(myName, "ERROR");
                    else dataSingelton.isLogged.put(p, true);
                }

            case "LOGOUT":
                Pair<String, String> pair= new Pair<>(splitted[0], splitted[1]);
                if (dataSingelton.isLogged.isEmpty())
                    connections.send(myName,"ERROR");
                else if (dataSingelton.listOfUsers.contains(pair))
                    dataSingelton.isLogged.put(pair,false);

        case "FOLLOW":
            String[]splitted=res.split("\\s+");
            dataSingelton.followList.remove()

                else



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
