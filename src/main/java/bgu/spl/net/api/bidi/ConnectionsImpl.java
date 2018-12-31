package bgu.spl.net.api.bidi;
import bgu.spl.net.srv.ConnectionHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{

    private ConcurrentHashMap<Integer,ConnectionHandler<T>> connections;

    public ConnectionsImpl(){
        this.connections=new ConcurrentHashMap<>();
    }


    public void add(ConnectionHandler<T> handler,int clientCount) {
        connections.put(clientCount,handler);
    }

    public void remove(int connectionId) {
        connections.remove(connectionId);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(!connections.containsKey(connectionId))
            return false; //connection not found,return false or exception
        ConnectionHandler<T> clientHandler=connections.get(connectionId);
        clientHandler.send(msg);
        return true;
    }

    @Override
    public void broadcast(T msg) {
        connections.keySet().forEach(connectionId->send(connectionId,msg));
    }

    @Override
    public void disconnect(int connectionId) {
        if(!connections.containsKey(connectionId))
            return;
        ConnectionHandler<T> clientHandler=connections.get(connectionId);
        try {
            clientHandler.close(); //send the connection end if exists
        } catch (IOException e) {
            e.printStackTrace();
        }
        remove(connectionId);
    }
}