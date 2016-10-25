package pa1;

import java.net.*;
import java.io.*;

/**
 *
 * @author Joffrey Pannee
 */
public class Client {

    private Socket socket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private String[] args;

    private final int BYTE_LENGTH = 1024;

    public Client(String[] args) {
        this.args = args;

        if (this.args.length < 2) {
            System.err.print("Not enough arguments supplied to client object.");
            System.exit(1);
        }
        SetupSocket();

        try {
            String action = GetArg(1);
            switch (action.trim()) {
                case "upload":
                    break;
                case "download":
                    break;
                case "dir":
                    break;
                case "mkdir":
                    break;
                case "rmdir":
                    break;
                case "rm":
                    break;
                case "shutdown":
                    break;
                default:
                    System.err.print("Invalid argument passed to client object.");
                    System.exit(1);
                    break;
            }

            String msg = "";

            //Skip first arg intentionally
            for (int i = 1; i < args.length; i++) {
                msg += args[i] + " ";
            }
            outToServer.writeBytes(msg);
            socket.close();

        } catch (IOException e) {
            System.err.print("There was a connection error while trying to connect to the server.");
            System.exit(5);
        }

    }

    private String GetArg(int index) {
        if (args.length < index + 1) {
            System.err.print("Not enough arguments supplied to client object.");
            System.exit(2);
        }
        String arg = args[index];
        if (arg == null || arg.isEmpty()) {
            System.err.print("Not enough arguments supplied to client object.");
            System.exit(2);
        }
        return arg;
    }

    private void SetupSocket() {
        String hostAndPort = System.getenv("PA1_SERVER");
        System.out.println(hostAndPort);
        String[] splitHostAndPort = hostAndPort.split(":");
        if (splitHostAndPort.length < 2) {
            System.err.print("The server specified in environment variables is not properly formed (should be \"hostname:port\" without quotes).");
            System.exit(1);
        }
        Integer portNum = 0;
        try {
            portNum = Integer.valueOf(splitHostAndPort[1]);

        } catch (NumberFormatException e) {
            System.err.print("The server port specified in environment variables is not properly formed (port was not an integer).");
            System.exit(1);
        }
        try {
            socket = new Socket(splitHostAndPort[0], portNum);
            outToServer = new DataOutputStream(socket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.print("The server specified was unreachable.");
            System.exit(5);
        } catch (IOException e) {
            System.err.print("There was a connection error while trying to connect to the server.");
            System.exit(5);
        }
    }

    private void DoUpload() throws IOException {
        String pathOfFileClient = GetArg(2);
        File file = new File(pathOfFileClient);
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.print("The specified file was not found on the client.");
            System.exit(2);
        }
        String serverPathToSave = GetArg(3);
        outToServer.writeBytes("upload " + serverPathToSave);
        //outToServer.
        String response = inFromServer.readLine();
        if (response.equals("confirmed")) {
            byte[] fileBytes = new byte[BYTE_LENGTH];
            int bytesSent = 0;
            int readResult = -1;
            while ((readResult = fileStream.read(fileBytes, 0, BYTE_LENGTH)) != -1) {
                outToServer.write(fileBytes, 0, readResult);
                outToServer.flush();
                bytesSent += BYTE_LENGTH;
            }
        }else{            
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }

    }

}
