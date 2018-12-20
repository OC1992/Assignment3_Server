package bgu.spl.net.api.bidi;
import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BGSMessageEncoderDecoder implements MessageEncoderDecoder<String> {
    private byte [] decOpArr=new byte[2];
    private byte [] usArr=new byte[2];
    private int opIndex=0;
    private int userIndex=0;
    private short opcode=0;
    private short numOfUsers=0;
    private int zeroByteCount=0;
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public byte[] encode(String message) {
        String opcodeString=message.substring(0,message.indexOf(' '));
        String noFirstOp=message.substring(message.indexOf(' ')+1);
        short op=StringToOpcode(opcodeString);
        byte[]first=shortToBytes(op);
        List<Byte> byteList=new LinkedList<>();
        addBytesToList(byteList,first);
        switch (op){
            case 10:
                return setAckFrame(byteList,noFirstOp);
            case 9:
                return setNotificationFrame(byteList,noFirstOp);
            case 11:
                short messageOp=StringToOpcode(noFirstOp);
                byte[] messageOpByte=shortToBytes(messageOp);
                addBytesToList(byteList,messageOpByte);
                return ByteListTobyteArray(byteList);
        }
        return new byte[0];
    }

    private byte[] setAckFrame(List<Byte> byteList,String line){
        short secondOp=Short.parseShort(line.substring(0,line.indexOf(' ')));
        byte[]second=shortToBytes(secondOp);
        addBytesToList(byteList,second);
        String noSecondOp=line.substring(line.indexOf(' ')+1);
        short thirdOp=Short.parseShort(noSecondOp.substring(0,noSecondOp.indexOf(' ')));
        byte[]third=shortToBytes(thirdOp);
        addBytesToList(byteList,third);
        String allTheRest=noSecondOp.substring(noSecondOp.indexOf(' ')+1);
        switch (secondOp){
            case 4:
            case 7:
                byte[]stringByte=(allTheRest.substring(0,allTheRest.length()-1)).getBytes();
                addBytesToList(byteList,stringByte);
                byteList.add((byte)'0');
                return ByteListTobyteArray(byteList);
            case 8:
                String[] forthAndFifth = allTheRest.split("\\s+");
                short forthOp=Short.parseShort(forthAndFifth[0]);
                short fifthOp=Short.parseShort(forthAndFifth[1]);
                byte[]forth=shortToBytes(forthOp);
                byte[]fifth=shortToBytes(fifthOp);
                addBytesToList(byteList,forth);
                addBytesToList(byteList,fifth);
                return ByteListTobyteArray(byteList);
        }
        return new byte[0];
    }

    private byte[] setNotificationFrame(List<Byte> byteList,String line){
        char notificationType=line.charAt(0);
        byteList.add((byte)notificationType);
        String PostingUserAndContent=line.substring(line.indexOf(' ')+1);
        String PostingUser=PostingUserAndContent.substring(0,PostingUserAndContent.indexOf(' '))+"\0";
        String Content=PostingUserAndContent.substring(PostingUserAndContent.indexOf(' '))+"\0";
        addBytesToList(byteList,PostingUser.getBytes());
        addBytesToList(byteList,Content.getBytes());
        return ByteListTobyteArray(byteList);
    }
    @Override
    public String decodeNextByte(byte nextByte) {
        if(opIndex<decOpArr.length){
            decOpArr[opIndex++]=nextByte;
            if(opIndex==decOpArr.length)
                opcode=bytesToShort(decOpArr);
            if(opcode!=3 & opcode!=7)
                return null;
        }
        switch (opcode){
            case 1:
            case 2:
            case 6:
                return getPMLoginRegisterFrame(nextByte);
            case 3:
            case 7:
                return getLogoutUserListFrame();
            case 4:
                return getFollowFrame(nextByte);
            case 5:
            case 8:
                return getPostStatFrame(nextByte);
        }
        return null;
    }

    private String getPostStatFrame(byte nextByte){
        if(nextByte=='\0') {
            String result = opcodeToString(opcode) + " " + new String(bytes, 0, len, StandardCharsets.UTF_8);
            len=0;
            opIndex=0;
            return result;
        }
        pushByte(nextByte);
        return null;
    }


    private String getFollowFrame(byte nextByte) {
        if (userIndex == 0) {
            pushByte(nextByte);
            userIndex++;
            return null;
        }
        if (userIndex == 1 |userIndex==2 ) {
            usArr[userIndex - 1] = nextByte;
            if (userIndex == 2)
                numOfUsers = bytesToShort(usArr);
            userIndex++;
            return null;
        }
        if(nextByte=='\0'){
            zeroByteCount++;
            if(zeroByteCount==numOfUsers){
                String byteResult = new String(bytes, 0, len, StandardCharsets.UTF_8);
                char follow=byteResult.charAt(0);
                String result1= byteResult.substring(1);
                String result=opcodeToString(opcode)+" "+follow+" "+numOfUsers+result1;
                len=0;
                zeroByteCount=0;
                opIndex=0;
                userIndex=0;
                return result;
            }
        }
       pushByte(nextByte);
        return null;
    }

    private String getLogoutUserListFrame(){
        String result=opcodeToString(opcode);
        len = 0;
        opcode=0;
        opIndex=0;
        return result;
    }


    private String getPMLoginRegisterFrame(byte nextByte){
        if(nextByte=='\0'){
            zeroByteCount++;
            if(zeroByteCount==2) {
                String byteResult = new String(bytes, 0, len, StandardCharsets.UTF_8);
                String result = opcodeToString(opcode) + " " + byteResult;
                len = 0;
                zeroByteCount = 0;
                opIndex = 0;
                return result;
            }
        }
        else
            pushByte(nextByte);
        return null;
    }


    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }
    private String opcodeToString(short opcode){
        switch (opcode){
            case 1:
                return "REGISTER";
            case 2:
                return "LOGIN";
            case 3:
                return "LOGOUT";
            case 4:
                return "FOLLOW";
            case 5:
                return "POST";
            case 6:
                return "PM";
            case 7:
                return "USERLIST";
            case 8:
                return "STAT";
            case 9:
                return "NOTIFICATION";
            case 10:
                return "ACK";
            case 11:
                return "ERROR";
        }
        return null;
    }

    private short StringToOpcode(String s){
        switch (s){
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

    private void addBytesToList(List<Byte> byteList,byte[] bytes){
        for(int i=0;i<bytes.length;i++)
            byteList.add(bytes[i]);
    }

    private byte[] ByteListTobyteArray(List<Byte> byteList){
        byte[] output=new byte[byteList.size()];
        for (int i=0;i<byteList.size();i++)
            output[i]=byteList.get(i);
       return output;
    }
}
