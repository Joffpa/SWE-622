package pa2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements ServerCommands {

    public Server() {
        super();
    }

    @Override
    public long sendFileBytes(byte[] bytes, String relativePath, boolean append) {
        try {
            String fullPath = GetFullFilePath(relativePath);
            File file = new File(fullPath);
            FileOutputStream fileStream = new FileOutputStream(file, append);
            fileStream.write(bytes);
            return file.length();
        } catch (IOException e) {
            System.err.println("Server exception on upload");
            System.exit(2);
        }
        return 0L;
    }

    @Override
    public long getFileSize(String relativePath) {
        relativePath = FormatPath(relativePath);
        String fullPath = GetBaseDir() + relativePath;
        File file = new File(fullPath);
        if (file.exists()) {
            return file.length();
        } else {
            return 0;
        }
    }

    @Override
    public byte[] getFileBytes(String relativePath, int offset, int bytesize) {
        try {
            String fullPath = GetFullFilePath(relativePath);
            File file = new File(fullPath);
            if (file.exists()) {
                FileInputStream fileStream = new FileInputStream(file);
                if (offset > 0) {
                    fileStream.skip(offset);
                }
                byte[] fileBytes = new byte[bytesize];
                int readResult = fileStream.read(fileBytes, 0, bytesize);
                if (readResult > -1) {
                    return fileBytes;
                }
            }
        } catch (IOException ex) {
            System.err.println("Server exception on download");
            System.exit(2);
        }
        return null;
    }

    @Override
    public boolean shutdown() {
        System.out.println("Shutting down");
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            registry.unbind(Enums.SERVER.val());
            UnicastRemoteObject.unexportObject(this, false);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean rm(String relativePath) {
        System.out.println("Removing file: " + relativePath);
        relativePath = FormatPath(relativePath);
        String fullPath = GetBaseDir() + relativePath;
        File file = new File(fullPath);
        try {
            return file.exists() && file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rmdir(String relativePath) {
        System.out.println("Removing directory: " + relativePath);
        relativePath = FormatPath(relativePath);
        String fullPath = GetBaseDir() + relativePath;
        File folder = new File(fullPath);
        try {
            return folder.exists() && folder.delete();
        } catch (Exception e) {
            System.err.println("Server exception on rmdir");
            System.exit(2);
            return false;
        }
    }

    @Override
    public boolean mkdir(String relativePath) {
        try {
            System.out.println("Making directory: " + relativePath);
            relativePath = FormatPath(relativePath);
            String fullPath = GetBaseDir() + relativePath;
            File folder = new File(fullPath);
            if (folder.exists()) {
                return false;
            } else {
                folder.mkdir();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Server exception on mkdir");
            System.exit(2);
            return false;
        }
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

    private String GetFullFilePath(String relativeDir) {
        String formatted = relativeDir;
        if (!formatted.equals("\\")) {
            if (!formatted.startsWith("\\")) {
                formatted = "\\" + formatted;
            }
        }
        return GetBaseDir() + formatted;
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
            Server server = new Server();
            ServerCommands stub = (ServerCommands) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(Enums.SERVER.val(), stub);
            System.out.println("Server bound");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
