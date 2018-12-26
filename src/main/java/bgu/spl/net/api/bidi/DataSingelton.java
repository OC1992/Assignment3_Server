package bgu.spl.net.api.bidi;

import jdk.internal.net.http.common.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class DataSingelton {
    HashMap<Integer,Pair<String,String>> listOfUsers;
    HashMap<String,Integer> UsersToSend;
    HashMap<Integer,String> isLogged;
    HashMap <Integer, List<String>> followList;
    private static final Object lockDataSingelton = new Object();
    private static volatile DataSingelton instance = null;

    private DataSingelton (){
       this.listOfUsers= new HashMap<>();
        this.isLogged=new HashMap<>();
        this.followList=new HashMap<>();
        this.UsersToSend= new HashMap<>();

    }
    static DataSingelton getInstance() {
        DataSingelton result = instance;
        if (result == null) {
            synchronized (lockDataSingelton) {
                result = instance;
                if (result == null)
                    instance = result = new DataSingelton();
            }
        }
        return result;
    }
}
