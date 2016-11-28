package pa1;

import java.net.*;
import java.io.*;

/**
 *
 * @author Joffrey Pannee
 */
public class ServerThread extends Thread {

    private final Socket clientConnection;
    private BufferedReader inFromClientReader;
    private DataOutputStream outToClient;
    private ServerThreadMonitor monitor;
    private final int BYTE_LENGTH = 1024;

    public ServerThread(Socket clientConnection, ServerThreadMonitor monitor) {
        this.clientConnection = clientConnection;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            inFromClientReader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            inFromClientReader.ready();
            outToClient = new DataOutputStream(clientConnection.getOutputStream());
            String clientCommand = inFromClientReader.readLine();
            String relativeDir;
            switch (clientCommand) {
                case "upload":
                    relativeDir = inFromClientReader.readLine();
                    DoUploadToServer(relativeDir);
                    break;
                case "download":
                    relativeDir = inFromClientReader.readLine();
                    DoDownloadToClient(relativeDir);
                    break;
                case "dir":
                    relativeDir = inFromClientReader.readLine();
                    SendFileInfo(relativeDir);
                    break;
                case "mkdir":
                    relativeDir = inFromClientReader.readLine();
                    MakeDirectory(relativeDir);
                    break;
                case "rmdir":
                    relativeDir = inFromClientReader.readLine();
                    RemoveDirectory(relativeDir);
                    break;
                case "rm":
                    relativeDir = inFromClientReader.readLine();
                    RemoveFile(relativeDir);
                    break;
                case "shutdown":
                    monitor.endThread();
                    WriteString("Server will shutdown when all other active threads are complete. Number of active threads currently running: " + monitor.numThreads());
                    monitor.allThreadsComplete();
                    WriteString(Enums.Confirmed.msg());
                    System.exit(0);
                    break;
                default:
                    System.err.print("Invalid argument passed to client object.");
                    System.exit(1);
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid command sent from client");
        } catch (IOException e) {
            System.err.println("There was a connection error while trying to connect to the client.");
        } finally {
            try {
                clientConnection.close();
            } catch (IOException e) {
                System.err.println("There was an error while trying to close the connection.");
            }
            monitor.endThread();
        }
    }

    private void DoDownloadToClient(String filepath) throws IOException {
        System.out.println("Downloading file: " + filepath);
        if (!filepath.startsWith("\\")) {
            filepath = "\\" + filepath;
        }
        String fullPath = GetBaseDir() + filepath;
        File file = new File(fullPath);
        System.out.println(fullPath);
        long totalBytesToSend = 0;
        if (!file.exists()) {
            //3
            WriteString("File does not exist on server.");
            return;
        } else {
            totalBytesToSend = file.length();
            if (totalBytesToSend <= 0) {
                System.err.print("The specified file was not found on the client.");
                //3
                WriteString("File does not exist on server.");
                return;
            } else {
                //3
                WriteString(Enums.Confirmed.msg());
                //4
                WriteString(Long.toString(totalBytesToSend));
            }
        }

        FileInputStream fileStream = new FileInputStream(file);
        //5
        int bytesToSkip = Integer.valueOf(inFromClientReader.readLine());
        if (bytesToSkip > 0) {
            fileStream.skip(bytesToSkip);
        }

        byte[] fileBytes = new byte[BYTE_LENGTH];
        int readResult = -1;
        String response = "";
        while ((readResult = fileStream.read(fileBytes, 0, BYTE_LENGTH)) != -1) {
            //6 start download
            outToClient.write(fileBytes, 0, readResult);
            outToClient.flush();
            //7 confirm dl
            response = inFromClientReader.readLine();
            if (response == null || !response.equals(Enums.Confirmed.msg())) {
                throw new IOException();
            }
        }
        outToClient.write(new byte[1]);
        outToClient.flush();
        //8 confirm dl done
        response = inFromClientReader.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            System.out.println("File downloaded.");
        }
        fileStream.close();
    }

    private void DoUploadToServer(String filepath) throws IOException {
        System.out.println("Uploading file: " + filepath);
        filepath = FormatFilename(filepath);
        String fullPath = GetBaseDir() + filepath;
        File file = new File(fullPath);
        file.getParentFile().mkdirs();
        WriteString(Enums.Confirmed.msg());
        int bytesToSkip = Integer.valueOf(inFromClientReader.readLine());
        FileOutputStream fileOut = new FileOutputStream(file, bytesToSkip > 0);
        byte[] fileBytes = new byte[BYTE_LENGTH];
        InputStream inFromClient = clientConnection.getInputStream();
        int bytesRead;
        int totalBytesRead = 0;
        do {
            bytesRead = inFromClient.read(fileBytes);
            WriteString(Enums.Confirmed.msg());
            if (bytesRead != -1) {
                totalBytesRead += bytesRead;
            }
            fileOut.write(fileBytes);
        } while (bytesRead != -1 && bytesRead >= BYTE_LENGTH);
        System.out.println("Uploading complete");
        WriteString(Enums.Confirmed.msg());
        fileOut.close();
    }

    private String GetBaseDir() {
        String basePath = "";
        basePath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile().toString();
        while (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        return basePath;
    }

    private void SendFileInfo(String relativeDir) throws IOException {
        System.out.println("Sending folder information to client: " + relativeDir);
        relativeDir = FormatPath(relativeDir);
        String fullPath = GetBaseDir() + relativeDir;
        File folder = new File(fullPath);
        if (!folder.exists()) {
            WriteString("Specified folder not found.");
            return;
        } else {
            WriteString(Enums.Confirmed.msg());
        }
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            System.err.println("Invalid command sent from client");
        } else {
            String file = "";
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    file = listOfFile.getName();
                    WriteString(file);
                } else if (listOfFile.isDirectory()) {
                    file = "\\" + listOfFile.getName();
                    WriteString(file);
                }
            }
        }
        WriteString(Enums.End.msg());
    }

    private void MakeDirectory(String relativeDir) throws IOException {
        System.out.println("Making directory: " + relativeDir);
        relativeDir = FormatPath(relativeDir);
        String fullPath = GetBaseDir() + relativeDir;
        File folder = new File(fullPath);
        if (folder.exists()) {
            WriteString("Folder already exists on server.");
        } else {
            folder.mkdir();
            WriteString(Enums.Confirmed.msg());
        }
    }

    private void RemoveDirectory(String relativeDir) throws IOException {
        System.out.println("Removing directory: " + relativeDir);
        relativeDir = FormatPath(relativeDir);
        String fullPath = GetBaseDir() + relativeDir;
        File folder = new File(fullPath);
        boolean deleted = false;
        try {
            if (folder.exists()) {
                deleted = folder.delete();
            } else {
                WriteString("The folder could not be found.");
            }
        } catch (IOException e) {
            System.err.println("Error removing directory." + e.getMessage());
        }
        if (!deleted) {
            WriteString("The folder could not be deleted (are you sure it's empty?)");
        } else {
            WriteString(Enums.Confirmed.msg());
        }
    }

    private void RemoveFile(String relativeDir) throws IOException {
        System.out.println("Removing file: " + relativeDir);
        relativeDir = FormatFilename(relativeDir);
        String fullPath = GetBaseDir() + relativeDir;
        File file = new File(fullPath);
        boolean deleted = false;
        try {
            if (file.exists()) {
                deleted = file.delete();
            } else {
                WriteString("The file could not be found.");
            }
        } catch (IOException e) {
            System.err.println("Error removing file." + e.getMessage());
        }
        if (!deleted) {
            WriteString("The file could not be deleted.");
        } else {
            WriteString(Enums.Confirmed.msg());
        }
    }

    private String FormatFilename(String relativeDir) {
        String formatted = relativeDir;
        if (!formatted.startsWith("/")) {
            formatted = "/" + formatted;
        }
        return formatted;
    }

    private String FormatPath(String relativeDir) {
        String formatted = relativeDir;
        if (!formatted.equals("/")) {
            if (!formatted.startsWith("/")) {
                formatted = "/" + formatted;
            }
            if (!formatted.endsWith("/")) {
                formatted = formatted + "/";
            }
        }
        return formatted;
    }

    private void WriteString(String msg) throws IOException {
        outToClient.writeBytes(msg + "\n");
        outToClient.flush();
    }

}
