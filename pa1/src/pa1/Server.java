package pa1;

import java.net.*;
import java.io.*;

/**
 *
 * @author Joffrey Pannee
 */
public class Server {

    public Server(String[] args) {
        if (args.length < 3) {
            System.err.print("Not enough arguments supplied to server object.");
            System.exit(1);
        }
        try {
            String action = args[1];
            if (action.equals("start")) {
                int portNum = Integer.valueOf(args[2]);
                ServerSocket welcomeSocket = new ServerSocket(portNum);
                while (true) {
                    Socket clientConnection = welcomeSocket.accept();
                    new ServerThread(clientConnection, args).start();
                }
            } else {
                System.err.print("Invalid argument passed to server object.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.print("There was a connection error while trying to connect to the client.");
            System.exit(5);
        }
    }

}
