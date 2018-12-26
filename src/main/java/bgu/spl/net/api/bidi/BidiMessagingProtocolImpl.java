package bgu.spl.net.api.bidi;

import jdk.internal.net.http.common.Pair;

import java.util.LinkedList;

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
                    dataSingelton.UsersToSend.put(pair.first,myName);
                    connections.send(myName, "ACK");
                }

            case "LOGIN":

                if (!dataSingelton.listOfUsers.containsKey(myName)
                        | !dataSingelton.listOfUsers.get(myName).second.equals(splitted[1]))
                    connections.send(myName, "ERROR");
                else dataSingelton.isLogged.put(myName, splitted[0]);

//w8 for ack(?)
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
            String newMessage=null;
            LinkedList<String> usersToSend=new LinkedList<>();

            if (!dataSingelton.isLogged.containsKey(myName))
                connections.send(myName, "ERROR");
            else for (String s:splitted) {
                if (s.indexOf(0) == '@')
                    usersToSend.add(s);
                else
                    newMessage = newMessage + ' ' + s;
            }
            for (String s:usersToSend)
                if (dataSingelton.followList.get(dataSingelton.UsersToSend.get(s)).contains(dataSingelton.listOfUsers.get(myName).first))
                    connections.send(dataSingelton.UsersToSend.get(s),newMessage);


            case "PM":
                if (!dataSingelton.isLogged.containsKey(myName)|!dataSingelton.UsersToSend.containsKey(splitted[0]))
                    connections.send(myName, "ERROR");
                else
                    connections.send(dataSingelton.UsersToSend.get(splitted[0]),splitted[1]);


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
