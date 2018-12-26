package bgu.spl.net.api.bidi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private static volatile Database instance = null;
    private static final Object lockData = new Object();
    private ReentrantReadWriteLock rwl; //ReaderWriter pattern
    private ConcurrentHashMap<String,Boolean> loginHash;
    private ConcurrentHashMap<String,Integer> userToConnection;
    private ConcurrentHashMap<String,String> usersAndPasswords;
    private ConcurrentHashMap<String, Vector<String>> usersFollowingHash;
    private ConcurrentHashMap<String, Vector<String>> usersFollowersHash;
    private HashMap<String, Vector<String>> usersPostMessagesHash;
    private HashMap<String, Vector<String>> usersPmMessagesHash;
    private HashMap<String, Vector<String>> notSeenMessages;

    //A Thread safe constructor
    public static Database getInstance() {
        Database result = instance;
        if (result == null) {
            synchronized (lockData) {
                result = instance;
                if (result == null)
                    instance = result = new Database();
            }
        }
        return result;
    }


    private Database(){
        rwl =new ReentrantReadWriteLock(true);
        userToConnection =new ConcurrentHashMap<>();
        usersAndPasswords=new ConcurrentHashMap<>();
        usersFollowingHash =new ConcurrentHashMap<>();
        usersFollowersHash =new ConcurrentHashMap<>();
        loginHash =new ConcurrentHashMap<>();
        usersPostMessagesHash =new HashMap<>();
        usersPmMessagesHash =new HashMap<>();
        notSeenMessages=new HashMap<>();
    }

    public boolean addUser(String userName,String password,int connectionId){
        if(userToConnection.containsKey(userName))
            return false;
        if(usersAndPasswords.containsKey(userName))
            return false;
        usersAndPasswords.put(userName,password);
        loginHash.put(userName,false);
        usersFollowingHash.put(userName,new Vector<>());
        usersFollowersHash.put(userName,new Vector<>());
        usersPostMessagesHash.put(userName,new Vector<>());
        usersPmMessagesHash.put(userName,new Vector<>());
        notSeenMessages.put(userName,new Vector<>());
        return true;
    }

    public int getUserConnectionId(String userName){
        return userToConnection.get(userName);
    }
    public boolean userExist(String userName){
        return usersAndPasswords.containsKey(userName);
    }

    public boolean passCheck(String userName,String password){
        rwl.readLock().lock();
        if(!usersAndPasswords.containsKey(userName) || !usersAndPasswords.get(userName).equals(password))
            return false;
        rwl.readLock().unlock();
        return true;
        }


    public boolean isFollow(String user1,String user2){
        rwl.readLock().lock();
        boolean output=true;
        if(!usersFollowingHash.containsKey(user1))
            output=false;
        if(!usersFollowingHash.get(user1).contains(user2))
            return false;
        rwl.readLock().unlock();
        return output;
    }

    public void addFollowers(String user, List<String> followers){
        rwl.writeLock().lock();
        Vector<String> vector= usersFollowingHash.get(user);
        vector.addAll(followers);
        for(String person:followers)
            usersFollowersHash.get(person).add(user);
        rwl.writeLock().unlock();
    }

    public void deleteFollowers(String user, List<String> followers){
        rwl.writeLock().lock();
        Vector<String> vector= usersFollowingHash.get(user);
        vector.removeAll(followers);
        for(String person:followers)
            usersFollowersHash.get(person).remove(user);
        rwl.writeLock().unlock();
    }

    public Vector<String> getFollowers(String user){
        rwl.readLock().lock();
        Vector<String> output=usersFollowersHash.get(user);
        rwl.readLock().unlock();
        return output;
    }

    public void addPost(String user,String post){
        rwl.readLock().lock();
        usersPostMessagesHash.get(user).add(post);
        rwl.readLock().unlock();
    }

    public void addPm(String user,String content){
        rwl.readLock().lock();
        usersPmMessagesHash.get(user).add(content);
        rwl.readLock().unlock();
    }

    public List<String> getRegisteredUsers(){
        rwl.readLock().lock();
        List<String> output=new LinkedList<>(usersAndPasswords.keySet());
        rwl.readLock().unlock();
        return output;
    }
    public int getNumOfPosts(String user){
        rwl.readLock().lock();
        int output=usersPostMessagesHash.get(user).size();
        rwl.readLock().unlock();
        return output;
    }

    public int getNumOfFollowers(String user){
        rwl.readLock().lock();
        int output=usersFollowersHash.get(user).size();
        rwl.readLock().unlock();
        return output;
    }

    public int getNumOfFollowing(String user){
        rwl.readLock().lock();
        int output=usersFollowingHash.get(user).size();
        rwl.readLock().unlock();
        return output;
    }

    public boolean isLoggedIn(String user){
        rwl.readLock().lock();
        boolean output=true;
        if(!loginHash.containsKey(user))
            output=false;
        if(output)
            output= loginHash.get(user);
        rwl.readLock().unlock();
        return output;}


    public void setLoggedIn(String user,boolean login){
        rwl.writeLock().lock();
        if(loginHash.containsKey(user))
            loginHash.replace(user,login);
        rwl.writeLock().unlock();
    }

    public void setConnection(String user , int connectionId){
        rwl.writeLock().lock();
        userToConnection.put(user,connectionId);
        rwl.writeLock().unlock();
    }

    public void addNotSeenMessage(String user , String message){
        rwl.writeLock().lock();
        notSeenMessages.get(user).add(message);
        rwl.writeLock().unlock();
    }

    public Vector<String> getNotSeenMessages(String user){
        rwl.readLock().lock();
        Vector<String> output=notSeenMessages.get(user);
        rwl.readLock().unlock();
        return output;
    }

    public void removeConnection(String user){
        rwl.writeLock().lock();
        userToConnection.remove(user);
        rwl.writeLock().unlock();
    }




}
