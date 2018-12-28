package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args){


        Server.reactor(Runtime.getRuntime().availableProcessors(),
                        7777, //port
                        BidiMessagingProtocolImpl::new, //protocol factory
                        BGSMessageEncoderDecoder::new //message encoder decoder factory
                ).serve();
    }
}

