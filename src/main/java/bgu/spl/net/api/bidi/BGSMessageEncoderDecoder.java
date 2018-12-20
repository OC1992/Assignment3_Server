package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
        String opcodeString=message.substring(0,message.indexOf('-'));
        String noFirstOp=message.substring(message.indexOf('-'));
        short op=StringToOpcode(opcodeString);
        byte[]first=shortToBytes(op);
        switch (op){
            case 9:
                String secondOpString=noFirstOp.substring(0,noFirstOp.indexOf('-'));
                String noSecondOp=noFirstOp.substring(noFirstOp.indexOf('-'));
                String thirdOpString=noSecondOp.substring(0,noSecondOp.indexOf(' '));
                String allTheRest=noSecondOp.substring(noSecondOp.indexOf(' '))+'\0';
                short secondOp=StringToOpcode(secondOpString);
                byte[]second=shortToBytes(secondOp);
                short thirdOp=Short.parseShort(thirdOpString);
                byte[]third=shortToBytes(thirdOp);
                switch (secondOp){
                    case 4:
                    case 7:
                        byte[]stringByte=allTheRest.getBytes();
                        byte[]bytesTosend=new byte[6+stringByte.length];
                        bytesTosend[0]=first[0];
                        bytesTosend[1]=first[1];
                        bytesTosend[2]=second[0];
                        bytesTosend[3]=second[1];
                        bytesTosend[4]=third[0];
                        bytesTosend[5]=third[1];
                        for(int i=6;i<bytesTosend.length;i++)
                            bytesTosend[i]=stringByte[i-6];
                        return bytesTosend;
                    case 8:
                        String forthOpString=allTheRest.substring(0,allTheRest.indexOf(' '));
                        String noforthOpString=allTheRest.substring(allTheRest.indexOf(' '));
                        String fifthOpString=noforthOpString.substring(0,noforthOpString.indexOf(' '));
                        short forthOp=Short.parseShort(forthOpString);
                        short fifthOp=Short.parseShort(fifthOpString);
                        byte[]forth2=shortToBytes(forthOp);
                        byte[]fifth=shortToBytes(fifthOp);
                        byte[]bytesTosend2=new byte[10];
                        bytesTosend2[0]=first[0];
                        bytesTosend2[1]=first[1];
                        bytesTosend2[2]=second[0];
                        bytesTosend2[3]=second[1];
                        bytesTosend2[4]=third[0];
                        bytesTosend2[5]=third[1];
                        bytesTosend2[6]=forth2[0];
                        bytesTosend2[7]=forth2[1];
                        bytesTosend2[8]=fifth[1];
                        bytesTosend2[9]=fifth[1];
                        return bytesTosend2;
                }
            case 10:
                String secondOpString10=noFirstOp.substring(0,noFirstOp.indexOf(' '));
                String noSecondOp10=noFirstOp.substring(noFirstOp.indexOf(' '));
                short messageOp=StringToOpcode(secondOpString10);
                byte[]optional=noSecondOp10.getBytes();
                byte[] output=new byte[4+optional.length];
                byte[] messageOpBytes=shortToBytes(messageOp);
                output[0]=first[0];
                output[1]=first[1];
                output[2]=messageOpBytes[0];
                output[3]=messageOpBytes[1];
                for(int i=4;i<output.length;i++)
                    output[i]=optional[i-4];
                return output;
            case 11:
                String secondOpString11=noFirstOp.substring(0,noFirstOp.indexOf(' '));
                short messageOp11=StringToOpcode(secondOpString11);
                byte[] messageOp11Byte=shortToBytes(messageOp11);
                byte[] output11=new byte[4];
                output11[0]=first[0];
                output11[1]=first[1];
                output11[2]=messageOp11Byte[0];
                output11[3]=messageOp11Byte[1];
                return output11;
        }
        return new byte[0];
    }

    @Override
    public String decodeNextByte(byte nextByte) {
        if(opIndex<decOpArr.length){
            decOpArr[opIndex++]=nextByte;
            if(opIndex==decOpArr.length-1)
                opcode=bytesToShort(decOpArr);
            if(opcode!=6 & opcode!=3)
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


    private String getFollowFrame(byte nextByte){
        if(userIndex<3) {
            if(userIndex==0) {
                pushByte(nextByte);
                userIndex++;
                return null;
            }
            else {
                usArr[userIndex-1]=nextByte;
                if(userIndex==2)
                    numOfUsers=bytesToShort(usArr);
                return null;
            }
        }
        if(nextByte=='\0'){
            zeroByteCount++;
            if(zeroByteCount==numOfUsers){
                String byteResult = new String(bytes, 0, len, StandardCharsets.UTF_8);
                char follow=byteResult.charAt(0);
                String result1= byteResult.substring(1);
                String result=opcodeToString(opcode)+" "+follow+" "+result1;
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
        opIndex=0;
        return result;
    }


    private String getPMLoginRegisterFrame(byte nextByte){
        if(nextByte=='\0')
            zeroByteCount++;
        pushByte(nextByte);
        if(zeroByteCount==2){
            String byteResult = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String result=opcodeToString(opcode)+" "+byteResult;
            len = 0;
            zeroByteCount=0;
            opIndex=0;
            return result;
        }
        else
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

}
