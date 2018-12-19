package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BGSMessageEncoderDecoder implements MessageEncoderDecoder<String> {
    private byte [] opArr=new byte[2];
    private byte [] usArr=new byte[2];
    private int opIndex=0;
    private int userIndex=0;
    private short opcode=0;
    private short numOfUsers=0;

    private int zeroByteCount=0;


    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    @Override
    public String decodeNextByte(byte nextByte) {
        if(opIndex<opArr.length){
            opArr[opIndex++]=nextByte;
            if(opIndex==opArr.length-1)
                opcode=bytesToShort(opArr);
            if(opcode!=6 & opcode!=3)
                return null;
        }
        switch (opcode){
            case 1:
            case 2:
                return getLoginRegisterFrame(nextByte);
            case 3:
            case 6:
                return getLogoutUserListFrame();
            case 4:
                return getFollowFrame(nextByte);
            case 5:

        }

    }

    private String getPostFrame(byte nextByte){
        if(nextByte=='\0') {
            String result = opcode + " " + new String(bytes, 0, len, StandardCharsets.UTF_8);
            return
        }

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
                return opcode+" "+follow+" "+result1;
            }
        }
       pushByte(nextByte);
        return null;
    }

    private String getLogoutUserListFrame(){
        String result=""+opcode;
        len = 0;
        opIndex=0;
        return result;
    }


    private String getLoginRegisterFrame(byte nextByte){
        if(nextByte=='\0')
            zeroByteCount++;
        pushByte(nextByte);
        if(zeroByteCount==2){
            String byteResult = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String result=opcode+" "+byteResult;
            len = 0;
            zeroByteCount=0;
            opIndex=0;
            return result;
        }
        else
            return null;


    }
    @Override
    public byte[] encode(String message) {
        return new byte[0];
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
}
