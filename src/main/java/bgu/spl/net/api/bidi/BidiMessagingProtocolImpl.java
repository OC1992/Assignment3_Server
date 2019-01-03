package bgu.spl.net.api.bidi;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String> {
    private int clientId;
    private boolean login=false;
    private boolean terminate=false;
    private String userName;
    private Connections<String> connections;
    private final Database database;


    public BidiMessagingProtocolImpl(Database database){
        this.database=database;
    }
    /**
     * initiate the database if not already got initiate by other protocol
     * @param connectionId set up client connection id
     * @param connections set up connection class member
     */
    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = connections;
        this.clientId = connectionId;

    }

    /**
     * will let the ConnectionHandler to disconnect
     * @return true if user entered 'LOGOUT'
     */
    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
    /**
     * break down the message content for smaller pieces
     * and transfer to other methods for process
     * @param message message to process
     */
    @Override
    public void process(String message) {
        String first_token=message;
        if(message.contains(" "))
            first_token = message.substring(0, message.indexOf(' '));
        String restOfMessage = "";
        if (message.length() > 1) {
            restOfMessage = message.substring(message.indexOf(' ')+1);
        }
        short opcode = StringToOpcode(first_token);
        switch (opcode) {
            case 1:
                Register(restOfMessage);
                break;
            case 2:
                Login(restOfMessage);
                break;
            case 3:
                Logout();
                break;
            case 4:
                if(restOfMessage.charAt(0)=='0')
                    FollowUnfollow(restOfMessage.substring(restOfMessage.indexOf(' ')+1),true);
                else
                    FollowUnfollow(restOfMessage.substring(restOfMessage.indexOf(' ')+1),false);
                break;
            case 5:
                Post(restOfMessage);
                break;
            case 6:
                PM(restOfMessage);
                break;
            case 7:
                UserList();
                break;

            case 8:
                StatMessage(restOfMessage);
                break;
        }
    }


    /**
     * if not exist will register a new user into the system
     * @param message Register message to process
     */
    private void Register(String message) {
        String[] userAndPas = message.split("\\s+");
        boolean result=database.addUser(userAndPas[0], userAndPas[1]);
        if (!result)
            connections.send(clientId, "ERROR 1");
        else
            connections.send(clientId, "ACK 1");
    }


    /**
     * if not login to diffrent user or the current user is logged in
     * will allow login into the user if all it's credentials are correct
     * @param message Login message to process
     */
    private void Login(String message) {
        String[] userAndPas = message.split("\\s+");
        if (login | !database.userExist(userAndPas[0]) || !database.passCheck(userAndPas[0], userAndPas[1])|database.isLoggedIn(userAndPas[0]))
            connections.send(clientId, "ERROR 2");
        else {
            login = true;
            this.userName = userAndPas[0];
            database.setConnection(userName,clientId);
            database.setLoggedIn(userName,true);
            connections.send(clientId, "ACK 2");
            Vector<String> notSeen=database.getNotSeenMessages(userName);
            notSeen.forEach(msg->connections.send(clientId,msg));
            notSeen.clear();
        }

    }

    /**
     * disconnect a user from the server
     * if not login to any of the users will send an error message
     */
    private void Logout() {
        if (!login)
            connections.send(clientId, "ERROR 3");
        else {
            login = false;
            connections.send(clientId, "ACK 3");
            database.setLoggedIn(userName,false);
            database.removeConnection(userName);
            terminate=true;
            connections.disconnect(clientId);
        }

    }

    /**
     * deal with a message when a user wants to follow/unfollow other users
     * @param message FOLLOW message
     * @param follow 0 to follow, 1 to unfollow
     */
    private void FollowUnfollow(String message, boolean follow) {
        if (!login)
            connections.send(clientId, "ERROR 4");
        else {
            String usersOnly=message.substring(message.indexOf(' ')+1);
            String[] usersToFollowUnfollow = usersOnly.split("\\s+");
            List<String> canFollowUnfollowList = new LinkedList<>();
            for (String user : usersToFollowUnfollow) {
                if(!user.equals(userName) && database.userExist(user)) {
                    if (follow && !database.isFollow(userName, user))
                        canFollowUnfollowList.add(user);
                    if (!follow && database.isFollow(userName, user))
                        canFollowUnfollowList.add(user);
                }
            }
            if (canFollowUnfollowList.size() == 0) {
                connections.send(clientId, "ERROR 4");
                return;
            }
            StringBuilder stringBuilder=new StringBuilder();
            if (follow) {
                database.addFollowers(userName, canFollowUnfollowList);
                stringBuilder.append("ACK 4 ");
            }
            else{
                database.deleteFollowers(userName,canFollowUnfollowList);
                stringBuilder.append("ACK 4 ");
                }
            stringBuilder.append(canFollowUnfollowList.size());
            appendUsers(canFollowUnfollowList,stringBuilder);
            connections.send(clientId, stringBuilder.toString());
        }
    }

    /**
     * post a message in the server, will be sent to everyone who is tagged or following
     * this user
     * @param message Post Message
     */
    private void Post(String message){
        if(!login)
            connections.send(clientId,"ERROR 5");
        else {
          String toSend= "NOTIFICATION Public "+userName+" "+message ;
          List<String> tagged=new LinkedList<>();
          String[] splitted= message.split("@");

          for(int i=1;i<splitted.length;i=i+1) {
              String user;
              if(!splitted[i].contains(" "))
                  user = splitted[i];
              else
                  user=splitted[i].substring(0, splitted[i].indexOf(" "));
              if (database.userExist(user) && !tagged.contains(user))
                  tagged.add(user);
          }
          if(splitted.length>1 && tagged.size()==0) {
              connections.send(clientId, "ERROR 5");
              return;
          }
          Vector<String> followers=database.getFollowers(userName);
          followers.forEach(s->{
              tagged.remove(s);
              if(database.isLoggedIn(s)) {
                  if (!connections.send(database.getUserConnectionId(s), toSend))
                      database.addNotSeenMessage(s, toSend);
              }
              else
                  database.addNotSeenMessage(s,toSend);
          });
          tagged.forEach(s->{
              if (database.isLoggedIn(s)) {
                  if (!connections.send(database.getUserConnectionId(s), toSend))
                      database.addNotSeenMessage(s, toSend);
              }
              else
                  database.addNotSeenMessage(s, toSend);
          });
          database.addPost(userName,message);
          connections.send(clientId,"ACK 5");
        }
    }

    /**
     * send a private message to a specific user
     * @param message PM message
     */
    private void PM(String message){
        String userToSend;
        String content;
        if(!message.contains(" ")) {
            userToSend = message;
            content="";
        }
        else {
            userToSend = message.substring(0, message.indexOf(' '));
            content = message.substring(message.indexOf(' ') + 1);
        }
        String toSend= "NOTIFICATION PM "+userName+" "+content ;
        if(!login ||!database.userExist(userToSend)) {
            connections.send(clientId, "ERROR 6");
            return;
        }
        else if(database.isLoggedIn(userToSend)) {
                if (!connections.send(database.getUserConnectionId(userToSend), toSend))
                    database.addNotSeenMessage(userToSend, toSend);
        }
        else {
            database.addNotSeenMessage(userToSend,toSend);
        }
        database.addPm(userName,message);
        connections.send(clientId,"ACK 6");
    }

    /**
     * will send to the user who asked a list of all the users
     * who are registered in to the server
     */
    private void UserList(){
        if(!login){
            connections.send(clientId,"ERROR 7");
        }
        else {
            List<String> registerd=database.getRegisteredUsers();
            StringBuilder s=new StringBuilder("ACK 7 "+registerd.size());
            registerd.forEach(name->s.append(" ").append(name));
            connections.send(clientId,s.toString());
        }
    }

    /**
     * will send back to the user who activate this method the @user details:
     * number of posts, following count, followers count
     * @param user the user we will recover all the detail
     */
    private void StatMessage(String user){
        if(!login ||!database.userExist(user)){
            connections.send(clientId,"ERROR 8");
        }
        else {
            connections.send(clientId,"ACK 8 "+database.getNumOfPosts(user)+" "+database.getNumOfFollowers(user)+" "+database.getNumOfFollowing(user));
        }
    }

    /**
     * append to a string builder all the users, assist USERLIST message
     * @param users list of users
     * @param s stringbuilder
     */
    private void appendUsers(List<String> users,StringBuilder s){
        for (String user : users)
            s.append(" ").append(user);
    }

    /**
     * converts String into Opcode
     * @param s string to convert
     * @return  opcode based on the given string
     */
    private short StringToOpcode(String s) {
        switch (s) {
            case "REGISTER":
                return 1;
            case "LOGIN":
                return 2;
            case "LOGOUT":
                return 3;
            case "FOLLOW":
                return 4;
            case "POST":
                return 5;
            case "PM":
                return 6;
            case "USERLIST":
                return 7;
            case "STAT":
                return 8;
            case "NOTIFICATION":
                return 9;
            case "ACK":
                return 10;
            case "ERROR":
                return 11;
        }
        return 0;
    }
}
