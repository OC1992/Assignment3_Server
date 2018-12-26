package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.bidi.BGSMessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.Database;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;

public class NewsFeedServerMain {

    public static void main(String[] args) {

// you can use any server... 
        Server.threadPerClient(
                7777, //port
                BidiMessagingProtocolImpl::new, //protocol factory
                BGSMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
/*
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                () ->  new EchoProtocol(), //protocol factory
                LineMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
*/
    }
}
