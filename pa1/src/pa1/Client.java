package pa1;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final String UPLOAD_LOG_FILE = GetBaseDir() + "\\UploadLog.txt";
    private final String DOWNLOAD_LOG_FILE = GetBaseDir() + "\\DownloadLog.txt";

    public Client(String[] args) {
        this.args = args;
        if (this.args.length < 2) {
            System.err.print("Not enough arguments supplied to client object.");
            System.exit(1);
        }
        try {
            String action = GetArg(1);
            switch (action.trim()) {
                case "upload":
                    SetupSocket();
                    DoUpload();
                    break;
                case "download":
                    DoDownload();
                    break;
                case "dir":
                    SetupSocket();
                    DoDir();
                    break;
                case "mkdir":
                    SetupSocket();
                    DoMkDir();
                    break;
                case "rmdir":
                    SetupSocket();
                    DoRmDir();
                    break;
                case "rm":
                    SetupSocket();
                    DoRm();
                    break;
                case "shutdown":
                    SetupSocket();
                    DoShutdown();
                    break;
                default:
                    System.err.print("Invalid argument passed to client object.");
                    System.exit(1);
                    break;
            }
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("There was a connection error while trying to connect to the server.");
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
        if (arg.startsWith("\"")) {
            arg = arg.substring(1);
        }
        if (arg.endsWith("\"")) {
            arg = arg.substring(0, arg.length() - 2);
        }
        return arg;
    }

    private void SetupSocket() {
        String hostAndPort = System.getenv("PA1_SERVER");
        if (hostAndPort == null || hostAndPort.isEmpty()) {
            System.err.print("The server specified in environment variables is not set.");
            System.exit(1);
        }
        String[] splitHostAndPort = hostAndPort.split(":");
        if (splitHostAndPort.length < 2) {
            System.err.print("The server specified in environment variables is not properly formed (should be \"hostname:port\" without quotes). PA1_SERVER = " + hostAndPort);
            System.exit(1);
        }
        Integer portNum = 0;
        try {
            String portStr = splitHostAndPort[1].trim();
            portNum = Integer.valueOf(portStr);
        } catch (NumberFormatException e) {
            System.err.print("The server port specified in environment variables is not properly formed (port was not an integer). PA1_SERVER = " + hostAndPort);
            System.exit(1);
        }
        try {
            socket = new Socket(splitHostAndPort[0], portNum);
            outToServer = new DataOutputStream(socket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inFromServer.ready();
        } catch (UnknownHostException e) {
            System.err.print("The server specified was unreachable.");
            System.exit(5);
        } catch (IOException e) {
            System.err.print("There was a connection error while trying to connect to the server.");
            System.exit(5);
        }
    }

    private void DoDownload() {
        String pathOnServer = "";
        String pathToSave = "";
        String fullpathToSave = "";
        boolean downloadComplete = false;
        long bytesRecieved = 0;
        long totalBytesToDownload = 0;
        FileOutputStream fileOut = null;
        try {
            pathOnServer = GetArg(2);
            pathToSave = GetArg(3);
            if (!pathToSave.startsWith("\\")) {
                pathToSave = "\\" + pathToSave;
            }
            fullpathToSave = GetBaseDir() + pathToSave;
            File file = new File(fullpathToSave);
            bytesRecieved = PartialDownloadBytesSent(pathOnServer, fullpathToSave);
            if (file.exists() && bytesRecieved <= 0) {
                System.err.print("The specified file already exists on the client.");
                System.exit(2);
            }

            SetupSocket();
            //1
            WriteString("download");
            //2
            WriteString(pathOnServer);
            //3
            String response = inFromServer.readLine();
            if (!response.equals(Enums.Confirmed.msg())) {
                System.err.print(response);
                System.exit(2);
            }
            //4
            totalBytesToDownload = Long.valueOf(inFromServer.readLine());
            //write to server num bytes already downloaded
            //5
            WriteString(Long.toString(bytesRecieved));

            fileOut = new FileOutputStream(file, bytesRecieved > 0);
            InputStream inFromServerRaw = socket.getInputStream();
            byte[] fileBytes = new byte[BYTE_LENGTH];
            int bytesRead;
            int percentComplete = 0;
            do {
                //6 start download
                bytesRead = inFromServerRaw.read(fileBytes);
                //7 confirm dl
                WriteString(Enums.Confirmed.msg());
                if (bytesRead != -1) {
                    bytesRecieved += bytesRead;
                }
                fileOut.write(fileBytes);
                percentComplete = (int) (((double) bytesRecieved / totalBytesToDownload) * 100);
                if (percentComplete > 100) {
                    percentComplete = 100;
                }
                System.out.println("Downloading File : " + percentComplete + "%");
            } while (bytesRead != -1 && bytesRead >= BYTE_LENGTH);
            //8 confirm dl done
            WriteString(Enums.Confirmed.msg());
            RemovePartialDownloadFromLog(pathOnServer, fullpathToSave);
            downloadComplete = true;
            System.out.println("File downloaded.");
            fileOut.close();
        } catch (IOException e) {
            if (!downloadComplete) {
                RemovePartialDownloadFromLog(pathOnServer, fullpathToSave);
                LogPartialDownload(pathOnServer, fullpathToSave, bytesRecieved);
            }
            System.err.println("There was a connection error while trying to connect to the server.");
            System.exit(5);
        }
    }

    private void DoUpload() {
        String fullpath = "";
        String pathToSaveOnServer = "";
        long bytesSent = 0;
        boolean uploadComplete = false;

        try {
            String pathOfFileClient = GetArg(2);
            pathToSaveOnServer = GetArg(3);
            if (!pathOfFileClient.startsWith("\\")) {
                pathOfFileClient = "\\" + pathOfFileClient;
            }
            fullpath = GetBaseDir() + pathOfFileClient;
            File file = new File(fullpath);
            if (!file.exists()) {
                System.err.print("The specified file was not found on the client.");
                System.exit(2);
            }
            WriteString("upload");
            WriteString(pathToSaveOnServer);

            FileInputStream fileStream = null;
            try {
                fileStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                System.err.print("The specified file was not found on the client.");
                System.exit(2);
            }
            double totalBytesToSend = (double) file.length();
            if (totalBytesToSend <= 0) {
                System.err.print("The specified file was not found on the client.");
                System.exit(2);
            }
            String response = inFromServer.readLine();
            if (response.equals(Enums.Confirmed.msg())) {
                byte[] fileBytes = new byte[BYTE_LENGTH];
                bytesSent = 0;
                int readResult = -1;
                int percentComplete = 0;
                bytesSent = PartialUploadBytesSent(fullpath, pathToSaveOnServer);
                if (bytesSent > 0) {
                    fileStream.skip(bytesSent);
                }
                WriteString(Long.toString(bytesSent));
                while ((readResult = fileStream.read(fileBytes, 0, BYTE_LENGTH)) != -1 && totalBytesToSend > bytesSent) {
                    System.out.println(readResult);
                    outToServer.write(fileBytes, 0, readResult);
                    outToServer.flush();
                    response = inFromServer.readLine();
                    if (response == null || !response.equals(Enums.Confirmed.msg())) {
                        throw new IOException();
                    }
                    bytesSent += readResult;
                    percentComplete = (int) (((double) bytesSent / totalBytesToSend) * 100);
                    if (percentComplete > 100) {
                        percentComplete = 100;
                    }
                    System.out.println("Uploading File : " + percentComplete + "%");
                }
                outToServer.write(new byte[1]);
                outToServer.flush();
            } else {
                System.err.print("There was an error uploading the file to the server. ");
                System.exit(2);
            }
            response = inFromServer.readLine();
            System.out.println(response);
            if (response.equals(Enums.Confirmed.msg())) {
                RemovePartialUploadFromLog(fullpath, pathToSaveOnServer);
                uploadComplete = true;
                System.out.println("File uploaded.");
            } else {
                System.err.print("There was an error uploading the file to the server. ");
                System.exit(2);
            }
            fileStream.close();
        } catch (IOException e) {
            if (!uploadComplete) {
                RemovePartialUploadFromLog(fullpath, pathToSaveOnServer);
                LogPartialUpload(fullpath, pathToSaveOnServer, bytesSent);
            }
            System.err.println("There was a connection error while trying to connect to the server.");
            System.exit(5);
        }

    }

    private void DoDir() throws IOException {
        String relativeDirPathOnServer = GetArg(2);
        WriteString("dir");
        WriteString(relativeDirPathOnServer);
        String response = inFromServer.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            String fileList;
            while (true) {
                fileList = inFromServer.readLine();
                if (fileList.equalsIgnoreCase(Enums.End.msg())) {
                    break;
                }
                System.out.println(fileList);
            }
            System.out.println("<end>");
        } else if (response.equals(Enums.End.msg())) {
            System.out.println("<No files in directory>");
        } else {
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }
    }

    private void DoMkDir() throws IOException {
        String relativeDirPathOnServer = GetArg(2);
        WriteString("mkdir");
        WriteString(relativeDirPathOnServer);
        String response = inFromServer.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            System.out.println("Directory created on server: " + relativeDirPathOnServer);
        } else {
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }
    }

    private void DoRmDir() throws IOException {
        String relativeDirPathOnServer = GetArg(2);
        WriteString("rmdir");
        WriteString(relativeDirPathOnServer);
        String response = inFromServer.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            System.out.println("Directory removed from server: " + relativeDirPathOnServer);
        } else {
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }
    }

    private void DoRm() throws IOException {
        String relativeDirPathOnServer = GetArg(2);
        WriteString("rm");
        WriteString(relativeDirPathOnServer);
        String response = inFromServer.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            System.out.println("File removed from server: " + relativeDirPathOnServer);
        } else {
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }
    }

    private void DoShutdown() throws IOException {
        WriteString("shutdown");
        String response = inFromServer.readLine();
        System.out.println(response);
        response = inFromServer.readLine();
        if (response.equals(Enums.Confirmed.msg())) {
            System.out.println("Server shutting down.");
        } else {
            System.err.print("Error message from server: " + response);
            System.exit(2);
        }
    }

    private String GetBaseDir() {
        String basePath = "";
        basePath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile().toString();
        while (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        return basePath;
    }

    private void WriteString(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
    }

    private long PartialUploadBytesSent(String localFile, String uploadPath) {
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(UPLOAD_LOG_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(localFile + " " + uploadPath)) {
                        String bytesSentStr = line.replace(localFile + " " + uploadPath, "").trim();
                        return Long.valueOf(bytesSentStr);
                    }
                }
            }
        } catch (IOException e) {
        }
        return 0;
    }

    private void LogPartialUpload(String localFile, String uploadPath, long bytesUploaded) {
        try (PrintWriter writer = new PrintWriter(UPLOAD_LOG_FILE, "UTF-8")) {
            writer.println(localFile + " " + uploadPath + " " + bytesUploaded);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.err.print("There was an error logging the failed upload.");
            System.exit(2);
        }
    }

    private void RemovePartialUploadFromLog(String localFile, String uploadPath) {
        LinkedList<String> tempLog = new LinkedList();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(UPLOAD_LOG_FILE));
                    PrintWriter writer = new PrintWriter(UPLOAD_LOG_FILE, "UTF-8")) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(localFile + " " + uploadPath)) {
                        tempLog.add(line);
                    }
                }
                for (String log : tempLog) {
                    writer.println(log);
                }
            }
        } catch (IOException e) {
        }
    }

    private long PartialDownloadBytesSent(String downloadPathServer, String localFile) {
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(DOWNLOAD_LOG_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(downloadPathServer + " " + localFile)) {
                        String bytesSentStr = line.replace(downloadPathServer + " " + localFile, "").trim();
                        return Long.valueOf(bytesSentStr);
                    }
                }
            }
        } catch (IOException e) {
        }
        return 0;
    }

    private void LogPartialDownload(String downloadPathServer, String localFile, long bytesUploaded) {
        try (PrintWriter writer = new PrintWriter(DOWNLOAD_LOG_FILE, "UTF-8")) {
            writer.println(downloadPathServer + " " + localFile + " " + bytesUploaded);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.err.print("There was an error logging the failed upload.");
            System.exit(2);
        }
    }

    private void RemovePartialDownloadFromLog(String downloadPathServer, String localFile) {
        LinkedList<String> tempLog = new LinkedList();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(DOWNLOAD_LOG_FILE));
                    PrintWriter writer = new PrintWriter(DOWNLOAD_LOG_FILE, "UTF-8")) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(downloadPathServer + " " + localFile)) {
                        tempLog.add(line);
                    }
                }
                for (String log : tempLog) {
                    writer.println(log);
                }
            }
        } catch (IOException e) {
        }
    }

}
