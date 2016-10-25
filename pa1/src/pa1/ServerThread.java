package pa1;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Joffrey Pannee
 */
public class ServerThread extends Thread {

    private final Socket clientConnection;
    private final String[] args;
    BufferedInputStream inFromClient;
    BufferedReader inFromClientReader;
    DataOutputStream outToClient;

    private final int BYTE_LENGTH = 1024;

    public ServerThread(Socket clientConnection, String[] args) {
        this.clientConnection = clientConnection;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            inFromClient = new BufferedInputStream(clientConnection.getInputStream(), BYTE_LENGTH);
            inFromClientReader = new BufferedReader(new InputStreamReader(inFromClient, StandardCharsets.UTF_8));
            outToClient = new DataOutputStream(clientConnection.getOutputStream());
            String input = inFromClientReader.readLine();
            if (input == null || input.isEmpty()) {
                System.err.print("Invalid command sent from client: " + input);
                return;
            }
            String[] actionArray = input.split(" ");
            if (actionArray.length < 1) {
                System.err.print("Invalid command sent from client: " + input);
                return;
            }
            String action = actionArray[0];
            if (action == null || action.isEmpty()) {
                System.err.print("Invalid command sent from client: " + input);
                return;
            }
            switch (input) {
                case "upload":
                    if (actionArray.length < 2) {
                        System.err.print("Invalid command sent from client: " + input);
                        return;
                    }
                    DoUpload(actionArray[1]);
                    break;
                default:
                    break;
            }

            System.out.println(input);
        } catch (IOException e) {
            System.err.print("There was a connection error while trying to connect to the client.");
            System.exit(5);
        }
    }

    private void DoUpload(String filepath) throws IOException{
        outToClient.writeBytes("confirmed");
        String pathToSaveFile = inFromClientReader.readLine();
        System.out.println(pathToSaveFile); 
        
        byte[] fileBytes = new byte[BYTE_LENGTH];
        //inFromClient.

    }

}
