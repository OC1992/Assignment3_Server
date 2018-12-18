package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) throws Exception {
        NewsFeed feed = new NewsFeed();
        if (args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }



                Server.reactor(
                        Runtime.getRuntime().availableProcessors(),
                        7777, //port
                        () ->  new EchoProtocol(), //protocol factory
                        LineMessageEncoderDecoder::new //message encoder decoder factory
                ).serve();

    }


}

