package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args){

        Database database=Database.getInstance();

        Server.reactor(Integer.parseInt(args[1]),
                        Integer.parseInt(args[0]), //port
                ()->new BidiMessagingProtocolImpl(database), //protocol factory
                        BGSMessageEncoderDecoder::new //message encoder decoder factory
                ).serve();
    }
}

