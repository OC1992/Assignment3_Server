package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;

import java.io.IOException;

public interface Connections<T> {
    public void add(ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);
    public ConnectionHandler<T> get(int connectionId);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
