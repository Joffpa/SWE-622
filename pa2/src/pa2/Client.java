/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pa2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Client {

    private static final int BYTE_LENGTH = 1;

    public static void main(String args[]) {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            System.out.println("Starting Client");
            Registry registry = LocateRegistry.getRegistry(null);
            ServerCommands server = (ServerCommands) registry.lookup(Enums.SERVER.val());

            String command = GetArg(args, 0);
            String relativePath;

            switch (command.trim()) {
                case "upload":
                    break;
                case "download":
                    DoDownload(args, server);
                    break;
                case "dir":
                    relativePath = GetArg(args, 1);
                    String dirStr = server.dir(relativePath);
                    System.out.println("Directory of " + relativePath + ": ");
                    System.out.println(dirStr);
                    break;
                case "mkdir":
                    relativePath = GetArg(args, 1);
                    boolean dirCreated = server.mkdir(relativePath);
                    if (dirCreated) {
                        System.out.println("Directory created: " + relativePath);
                    } else {
                        System.err.println("There was an error creating the directory (it may already exist).");
                        System.exit(3);
                    }
                    break;
                case "rmdir":
                    relativePath = GetArg(args, 1);
                    if (server.rmdir(relativePath)) {
                        System.out.println("Directory deleted: " + relativePath);
                    } else {
                        System.err.println("There was an error deleting the directory (it may not exist or is not empty).");
                        System.exit(3);
                    }
                    break;
                case "rm":
                    relativePath = GetArg(args, 1);
                    if (server.rm(relativePath)) {
                        System.out.println("File deleted: " + relativePath);
                    } else {
                        System.err.println("There was an error deleting the file (it may not exist).");
                        System.exit(3);
                    }
                    break;
                case "shutdown":
                    if (server.shutdown()) {
                        System.out.println("Server Shutdown");
                    } else {
                        System.err.println("There was an error shutting down the server");
                        System.exit(3);
                    }
                    break;
                default:
                    System.err.print("Invalid argument passed to client object.");
                    System.exit(3);
                    break;
            }
        } catch (NotBoundException e) {
            System.err.println("The server is not running.");
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Remote exception:");
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void DoDownload(String[] args, ServerCommands server) {
        try {
            String relativePathOnServer = GetArg(args, 1);
            long fileLength = server.getFileSize(relativePathOnServer);
            if (fileLength > 0) {
                String relativePathOnClient = GetArg(args, 2);
                String fullpath = GetFullPath(relativePathOnClient);
                File file = new File(fullpath);
            System.out.println(fullpath);
                FileOutputStream fileOut;
                if (file.exists()) {
                    fileOut = new FileOutputStream(file, true);
                } else {
                    fileOut = new FileOutputStream(file, false);
                }
               
                boolean fileIncomplete = true;
                while (fileIncomplete) {
                    long bytesAlreadyInFile = file.length();
                    ArrayList<Byte> bytesToAppend = server.getFileBytes(relativePathOnServer, (int) bytesAlreadyInFile, BYTE_LENGTH);
                    if (bytesToAppend != null) {
                        System.out.println("Its Not Null");
                        byte[] byteArray = new byte[BYTE_LENGTH];
                        for(int i = 0; i < BYTE_LENGTH && i < bytesToAppend.size(); i ++){
                            byteArray[i] = bytesToAppend.get(i);
                        }
                        fileOut.write(byteArray);
                    } else {
                        System.out.println("Its Null");
                        fileIncomplete = false;
                    }
                    int percentComplete = (int) (((double) file.length() / fileLength) * 100);
                    if (percentComplete > 100) {
                        percentComplete = 100;
                    }
                    System.out.println("Downloading File : " + percentComplete + "%");
                }
            } else {
                System.err.println("The file does not exist on the server");
                System.exit(5);
            }

        } catch (FileNotFoundException e) {
            System.err.println("There was an error trying to find the file specified");
            System.exit(5);
        } catch (RemoteException e) {
            System.err.println("There was an error trying to connect to the server.");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("There was an error handling the file.");
            System.exit(5);
        }
    }

    private static String GetArg(String[] args, int index) {
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

    private static String GetFullPath(String relativePath) {
        if (!"\\".equals(relativePath)) {
            if (relativePath.startsWith("\\")) {
                relativePath = relativePath.substring(1);
            }
        }
        return GetBaseDir() + relativePath;
    }

    private static String GetBaseDir() {
        String basePath = "";
        basePath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile().toString();
        while (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        return basePath;
    }

}
