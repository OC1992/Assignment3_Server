package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        Server.threadPerClient(
              Integer.parseInt(args[0]), //port
                BidiMessagingProtocolImpl::new, //protocol factory
               BGSMessageEncoderDecoder::new //message encoder decoder factory

       ).serve();

    }
}
