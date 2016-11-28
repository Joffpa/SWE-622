package pa2;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements ServerCommands {

    public Server() {
        super();
    }

    @Override
    public String dir(String relativePath) {
        System.out.println("Sending folder information to client: " + relativePath);
        relativePath = FormatPath(relativePath);
        String fullPath = GetBaseDir() + relativePath;
        File folder = new File(fullPath);
        if (!folder.exists()) {
            return "Specified folder not found.";
        } else {
            File[] listOfFiles = folder.listFiles();
            StringBuilder dir = new StringBuilder();
            if (listOfFiles == null) {
                return "There was an error searching for that directory";
            } else {
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        dir.append(listOfFile.getName()).append("\n");
                    } else if (listOfFile.isDirectory()) {
                        dir.append("\\").append(listOfFile.getName()).append("\n");
                    }
                }
            }
            return dir.toString();
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

    public static void main(String[] args) {
        System.out.println("Server starting");
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            String name = "Server";
            Server server = new Server();
            ServerCommands stub = (ServerCommands) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server bound");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }
}
