package bgu.spl.net.api.bidi;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private ConcurrentHashMap<String,Integer> UserToConnection;
    private ConcurrentHashMap<String,String> usersAndPasswords;
    private ConcurrentHashMap<String, Vector<String>> followingHash;
    private ConcurrentHashMap<String, Vector<String>> followersingHash;
    private HashMap<String, Vector<String>> usersPostMessages;
    private HashMap<String, Vector<String>> usersPmMessages;



    public Database(){
        UserToConnection=new ConcurrentHashMap<>();
        usersAndPasswords=new ConcurrentHashMap<>();
        followingHash=new ConcurrentHashMap<>();
        followersingHash=new ConcurrentHashMap<>();
        usersPostMessages=new HashMap<>();
        usersPmMessages=new HashMap<>();
    }

    public boolean addUser(String userName,String password,int connectionId){
        if(UserToConnection.containsKey(userName))
            return false;
        UserToConnection.put(userName,connectionId);
        if(usersAndPasswords.containsKey(userName))
            return false;
        usersAndPasswords.put(userName,password);
        followingHash.put(userName,new Vector<>());
        followersingHash.put(userName,new Vector<>());
        usersPostMessages.put(userName,new Vector<>());
        usersPmMessages.put(userName,new Vector<>());
        return true;
    }

    public int getUserConnectionId(String userName){
        return UserToConnection.get(userName);
    }
    public boolean userExist(String userName){
        return UserToConnection.containsKey(userName) & usersAndPasswords.containsKey(userName);
    }

    public boolean passCheck(String userName,String password){
        if(!usersAndPasswords.containsKey(userName) || !usersAndPasswords.get(userName).equals(password))
            return false;
        return true;
        }

    public boolean isFollow(String user1,String user2){
        if(!followingHash.containsKey(user1))
            return false;
        return followingHash.get(user1).contains(user2);
    }

    public void addFollowers(String user, List<String> followers){
        Vector<String> vector=followingHash.get(user);
        vector.addAll(followers);
        for(String person:followers)
            followersingHash.get(person).add(user);
    }

    public void deleteFollowers(String user, List<String> followers){
        Vector<String> vector=followingHash.get(user);
        vector.removeAll(followers);
        for(String person:followers)
            followersingHash.get(person).remove(user);
    }

    public Vector<String> getFollowers(String user){
        return followersingHash.get(user);
    }

    public void addPost(String user,String post){
        usersPostMessages.get(user).add(post);
    }

    public void addPm(String user,String content){
        usersPmMessages.get(user).add(content);
    }

    public List<String> getRegisteredUsers(){
        return new LinkedList<>(usersAndPasswords.keySet());
    }
    public int getNumOfPosts(String user){
        return usersPostMessages.get(user).size();
    }

    public int getNumOfFollowers(String user){
        return followersingHash.get(user).size();
    }

    public int getNumOfFollowing(String user){
        return followingHash.get(user).size();
    }


}
