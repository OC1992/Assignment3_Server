package bgu.spl.net.api.bidi;

public interface BidiMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionId, Connections<String> connections);
    
    void process(String message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
