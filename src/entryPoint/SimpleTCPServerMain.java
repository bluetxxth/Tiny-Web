package entryPoint;

import java.util.Scanner;

import logic.SimpleTCPServer;


public class SimpleTCPServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//prompt the user for port
		int port = 8080;
		
		if (args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("Non integer value for port number, using " + port + " instead");
            }
        }
		
		//create a new server object
		SimpleTCPServer server = new SimpleTCPServer(port);

		System.out.println("Starting server listening on port " + port);
		
		try {
			//accept connections and process requests
			server.runServer();
			
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

}
