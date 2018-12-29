package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        Database database=new Database();
        Server.threadPerClient(
              Integer.parseInt(args[0]), //port
                ()->new BidiMessagingProtocolImpl(database), //protocol factory
               BGSMessageEncoderDecoder::new //message encoder decoder factory

       ).serve();

    }
}
