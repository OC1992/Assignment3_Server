package bgu.spl.net.api.bidi;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String> {
    private int clientId;
    private Database database;
    private boolean login;
    private boolean terminate;
    private String userName;
    private Connections<String> connections;


    public BidiMessagingProtocolImpl(Database database) {
        this.database = database;
        this.login = false;
        this.terminate=false;
        this.userName = "";
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = connections;
        this.clientId = connectionId;
    }

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
                StatMessage();
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

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

    private void Register(String message) {
        String[] userAndPas = message.split("\\s+");
        if (database.userExist(userAndPas[0]))
            connections.send(clientId, "ERROR 1");
        else {
            database.addUser(userAndPas[0], userAndPas[1], clientId);
            connections.send(clientId, "ACK 1");
        }
    }

    private void Login(String message) {
        String[] userAndPas = message.split("\\s+");
        if (login | !database.passCheck(userAndPas[0], userAndPas[1]))
            connections.send(clientId, "ERROR 2");
        else {
            login = true;
            this.userName = userAndPas[0];
            database.setConnection(userName,clientId);
            connections.send(clientId, "ACK 2");
            Vector<String> notSeen=database.getNotSeenMessages(userName);
            notSeen.forEach(msg->connections.send(clientId,msg));
            notSeen.clear();
        }

    }

    private void Logout() {
        if (!login)
            connections.send(clientId, "ERROR 3");
        else {
            login = false;
            connections.send(clientId, "ACK 3");
            database.setLoggedIn(userName,false);
            database.removeConnection(userName);
            connections.disconnect(clientId);
            terminate=true;
        }

    }

    private void FollowUnfollow(String message, boolean follow) {
        if (!login)
            connections.send(clientId, "ERROR 4");
        else {
            String usersOnly=message.substring(message.indexOf(' ')+1);
            String[] usersToFollowUnfollow = usersOnly.split("\\s+");
            List<String> canFollowUnfollowList = new LinkedList<>();
            for (String user : usersToFollowUnfollow) {
                if (follow && !database.isFollow(userName, user))
                    canFollowUnfollowList.add(user);
                if (!follow && database.isFollow(userName, user))
                    canFollowUnfollowList.add(user);
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

    private void Post(String message){
        if(!login)
            connections.send(clientId,"ERROR 5");
        else {
          String toSend= "NOTIFICATION Public "+userName+" "+message ;
          List<String> tagged=new LinkedList<>();
          String[] splitted= message.split("@");
          for(int i=1;i<splitted.length;i=i+1)
              tagged.add(splitted[i].substring(0,splitted[i].indexOf(" ")));
          Vector<String> followers=database.getFollowers(userName);
          followers.forEach(s->{
              tagged.remove(s);
              if(database.isLoggedIn(s))
                  connections.send(database.getUserConnectionId(s),toSend);
              else
                  database.addNotSeenMessage(s,toSend);
          });
          tagged.forEach(s->{
              if (database.isLoggedIn(s))
                  connections.send(database.getUserConnectionId(s), toSend);
              else
                  database.addNotSeenMessage(s, toSend);

          });
          database.addPost(userName,message);
          connections.send(clientId,"ACK 5");
        }
    }

    private void PM(String message){
        String userToSend=message.substring(0,message.indexOf(' '));
        String content=message.substring(message.indexOf(' ')+1);
        String toSend= "NOTIFICATION PM "+userName+" "+content ;
        if(!login ||!database.userExist(userToSend))
            connections.send(clientId,"ERROR 5");
        else if(database.isLoggedIn(userToSend))
                connections.send(database.getUserConnectionId(userToSend),toSend);
        else {
            database.addNotSeenMessage(userToSend,toSend);
        }
        database.addPm(userName,message);
        connections.send(clientId,"ACK 6");
    }


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

    private void StatMessage(){
        if(!login ||!database.userExist(userName)){
            connections.send(clientId,"ERROR 8");
        }
        else {
            connections.send(clientId,"ACK 8 "+database.getNumOfPosts(userName)+" "+database.getNumOfFollowing(userName)+" "+database.getNumOfFollowers(userName));
        }
    }


    private void appendUsers(List<String> users,StringBuilder s){
        for (String user : users)
            s.append(" ").append(user);
    }
}
