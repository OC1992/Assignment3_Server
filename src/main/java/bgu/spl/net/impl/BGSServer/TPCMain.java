package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) throws Exception {
        NewsFeed feed = new NewsFeed();
        if (args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }

        /*
        Server.threadPerClient(
              7777, //port
               () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
               ObjectEncoderDecoder::new //message encoder decoder factory
       ).serve();
       */
    }
}
