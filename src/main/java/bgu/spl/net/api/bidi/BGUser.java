package bgu.spl.net.api.bidi;
import java.util.Vector;

/**
 * this class represents a registered user in the server.
 */
public class BGUser {
    private final String userName;
    private final String password;
    private boolean login;
    private int connectionId;
    private final Vector<String> following;
    private final Vector<String> followers;
    private final Vector<String> privateMessages;
    private final Vector<String> publicMessages;
    private final Vector<String> notSeenMessages;


    public BGUser(String userName,String password){
        this.userName=userName;
        this.password=password;
        this.login=false;
        this.connectionId=-1;
        this.followers=new Vector<>();
        this.following=new Vector<>();
        this.publicMessages=new Vector<>();
        this.privateMessages=new Vector<>();
        this.notSeenMessages=new Vector<>();
    }

    /**
     * @param message Pm message to add
     */
    public void addPm(String message){
        privateMessages.add(message);
    }

    /**
     * @param message Post message to add
     */
    public void addPost(String message){
        publicMessages.add(message);
    }

    /**
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the user's login state
     */
    public boolean isLogin() {
        return login;
    }

    /**
     * @param login set the login to state to @login
     */
    public void setLogin(boolean login) {
        this.login = login;
    }

    /**
     * @return get all user's following list
     */
    public Vector<String> getFollowing() {
        return following;
    }


    /**
     * @return get all user's follwers list
     */
    public Vector<String> getFollowers() {
        return followers;
    }

    /**
     * @return get connection id of user
     */
    public int getConnectionId() {
        return connectionId;
    }

    /**
     * @param connectionId set connection id to @connectionId
     */
    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * @param other other user in the database
     * @return true if the current user follows @other
     */
    public boolean isFollow(String other){
        return following.contains(other);
    }


    /**
     * @param list list of users to add to following list
     */
    public void addFollowing(Vector<String> list){
        following.addAll(list);
    }


    /**
     * @param list of users to delete from following list
     */
    public void deleteFollowing(Vector<String> list){
        following.removeAll(list);
    }


    /**
     * @return the user's public messages
     */
    public Vector<String> getPublicMessages() {
        return publicMessages;
    }

    /**
     * @param message not seen message to add
     */
    public void addNotSeen(String message){
        notSeenMessages.add(message);
    }

    /**
     * @return the user's not seen messages
     */
    public Vector<String> getNotSeenMessages(){
        return notSeenMessages;
    }


    /**
     * @param user add a follower @user to followers list
     */
    public void addFollowed(String user){
        followers.add(user);
    }

    /**
     * @param user removes a follower @user from followers list
     */
    public void removeFollowed(String user){
        followers.remove(user);
    }
}
