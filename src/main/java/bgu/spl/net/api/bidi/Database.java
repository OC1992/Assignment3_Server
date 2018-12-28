package bgu.spl.net.api.bidi;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  Singleton class that holds all the data of the server: users, passwords, posts, pm
 *  and the specific connectionId when user log in.
 *  in general all the methods protected with read/write pattern. hash maps who can be access from couple
 *  of threads are concurrent- meaning thread safe.
 */
public class Database {
    private static volatile Database instance = null;
    private static final Object lockData = new Object();
    private ReadWriteLock rwl; //ReaderWriter pattern
    private ConcurrentHashMap<String,BGUser> nameToBGUser;


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

    //Private Constructor (will only run one time)
    private Database(){
        rwl =new ReentrantReadWriteLock(true);
        nameToBGUser=new ConcurrentHashMap<>();
    }

    /**
     *  if not exists already,adding the user into the database
     * @param userName user
     * @param password password
     * @return return if the operation succeed
     */
    public boolean addUser(String userName,String password){
        boolean output=true;
        rwl.writeLock().lock();
        if(nameToBGUser.containsKey(userName))
            output=false;
        else
            nameToBGUser.put(userName,new BGUser(userName,password));
        rwl.writeLock().unlock();
        return output;
    }

    /**
     * will return the user connectionId if he is log in
     * @param userName user
     * @return -1 if not found, if found return connectionId
     */
    public int getUserConnectionId(String userName){
        int output=-1;
        rwl.readLock().lock();
        if(userExist(userName))
            output= nameToBGUser.get(userName).getConnectionId();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * check if user is exist in the database
     * @param userName user
     * @return true if exist, false if not
     */
    public boolean userExist(String userName){
        rwl.readLock().lock();
        boolean output= nameToBGUser.containsKey(userName);
        rwl.readLock().unlock();
        return output;
    }


    /**
     * checks if the password of the user is valid
     * @param userName user
     * @param password password
     * @return true if password is correct and false if user not exist or password incorrect
     */
    public boolean passCheck(String userName,String password){
        boolean output=true;
        rwl.readLock().lock();
        if(!userExist(userName) || !nameToBGUser.get(userName).getPassword().equals(password))
            output=false;
        rwl.readLock().unlock();
        return output;
        }


    /**
     * checks if user1 following user2
     * @param user1 user1
     * @param user2 user2
     * @return true if he follows, false if user1 doesn't exist or user1 not following
     */
    public boolean isFollow(String user1,String user2){
        rwl.readLock().lock();
        boolean output=true;
        if(!userExist(user1) || !userExist(user2) ||!nameToBGUser.get(user1).isFollow(user2))
            output=false;
        rwl.readLock().unlock();
        return output;
    }

    /**
     * if exists add followers to user followers vector
     * @param user user
     * @param followers vector of new followers
     */
    public void addFollowers(String user, List<String> followers){
        rwl.writeLock().lock();
        if(userExist(user)) {
            Vector<String> toAdd = new Vector<>();
            for (String person : followers)
                if(userExist(person)){
                    toAdd.add(person);
                    nameToBGUser.get(person).addFollowed(user);
                }
        nameToBGUser.get(user).addFollowing(toAdd);
        }
        rwl.writeLock().unlock();
    }

    /**
     * delete followers from user's vector
     * @param user user
     * @param followers vector of followers to delete
     */
    public void deleteFollowers(String user, List<String> followers){
        rwl.writeLock().lock();
        if(userExist(user)) {
            Vector<String> toDelete=new Vector<>();
            for (String person : followers)
                if(userExist(person)) {
                    toDelete.add(person);
                    nameToBGUser.get(person).removeFollowed(user);
                }
        nameToBGUser.get(user).deleteFollowing(toDelete);
        }
        rwl.writeLock().unlock();
    }

    /**
     * if user exists will return his followers  vector
     * @param user user
     * @return if exist - followers. if not- empty vector
     */
    public Vector<String> getFollowers(String user){
        Vector<String> output=new Vector<>();
        rwl.readLock().lock();
        if(userExist(user))
            output=nameToBGUser.get(user).getFollowers();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * save the post into the database
     * @param user user
     * @param post post to save
     */
    public void addPost(String user,String post){
        rwl.readLock().lock();
        if(userExist(user))
            nameToBGUser.get(user).addPost(post);
        rwl.readLock().unlock();
    }

    /**
     * save thr pm into the database
     * @param user user
     * @param content pm content to save
     */
    public void addPm(String user,String content){
        rwl.readLock().lock();
        if(userExist(user))
            nameToBGUser.get(user).addPm(content);
        rwl.readLock().unlock();
    }

    /**
     * @return all the registered users
     */
    public List<String> getRegisteredUsers(){
        rwl.readLock().lock();
        List<String> output=new LinkedList<>(nameToBGUser.keySet());
        rwl.readLock().unlock();
        return output;
    }

    /**
     * @param user user
     * @return the user's number of posts
     */
    public int getNumOfPosts(String user){
        int output=-1;
        rwl.readLock().lock();
        if(userExist(user))
            output=nameToBGUser.get(user).getPublicMessages().size();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * @param user user
     * @return the user's followers number
     */
    public int getNumOfFollowers(String user){
        int output=-1;
        rwl.readLock().lock();
        if(userExist(user))
            output=nameToBGUser.get(user).getFollowers().size();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * @param user user
     * @return the user's following number
     */
    public int getNumOfFollowing(String user){
        int output=-1;
        rwl.readLock().lock();
        if(userExist(user))
            output=nameToBGUser.get(user).getFollowing().size();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * @param user user
     * @return true if user logged in, false if not exist or logged out
     */
    public boolean isLoggedIn(String user){
        rwl.readLock().lock();
        boolean output=true;
        if(!userExist(user) ||!nameToBGUser.get(user).isLogin())
            output=false;
        rwl.readLock().unlock();
        return output;
    }


    /**
     * change login state
     * @param user user
     * @param login true if login , false if not login
     */
    public void setLoggedIn(String user,boolean login){
        rwl.writeLock().lock();
        if(userExist(user))
            nameToBGUser.get(user).setLogin(login);
        rwl.writeLock().unlock();
    }

    /**
     * upon login set connection id to a specific user
     * @param user user
     * @param connectionId connection if
     */
    public void setConnection(String user , int connectionId){
        rwl.writeLock().lock();
        if(userExist(user))
            nameToBGUser.get(user).setConnectionId(connectionId);
        rwl.writeLock().unlock();
    }

    /**
     * add a message to the not seen vector of the user
     * @param user user
     * @param message message to add
     */
    public void addNotSeenMessage(String user , String message){
        rwl.writeLock().lock();
        if(userExist(user))
            nameToBGUser.get(user).addNotSeen(message);
        rwl.writeLock().unlock();
    }

    /**
     * @param user
     * @return all not seen messages of the user
     */
    public Vector<String> getNotSeenMessages(String user){
        Vector<String> output=new Vector<>();
        rwl.readLock().lock();
        if(userExist(user))
            output=nameToBGUser.get(user).getNotSeenMessages();
        rwl.readLock().unlock();
        return output;
    }

    /**
     * change the users connectionid to -1 until the next time he will log in
     * @param user user
     */
    public void removeConnection(String user){
        rwl.writeLock().lock();
        if (userExist(user))
            nameToBGUser.get(user).setConnectionId(-1);
        rwl.writeLock().unlock();
    }
}
