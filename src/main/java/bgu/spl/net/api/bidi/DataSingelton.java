package bgu.spl.net.api.bidi;

import jdk.internal.net.http.common.Pair;

import java.util.HashMap;
import java.util.LinkedList;

public class DataSingelton {
    private LinkedList<Pair<String,String>> listOfUsers;
    private HashMap<Pair<String,String>,Boolean> isLogged;
    private HashMap <Pair<String,String>,String> followList;
    private static final Object lockDataSingelton = new Object();
    private static volatile DataSingelton instance = null;

    private DataSingelton (){
       this.listOfUsers= new LinkedList<Pair<String,String>>();
        this.isLogged=new HashMap<>();
        this.followList=new HashMap<>();
    }
    public static DataSingelton getInstance() {
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
