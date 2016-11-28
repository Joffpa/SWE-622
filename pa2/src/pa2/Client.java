/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pa2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private final int BYTE_LENGTH = 1024;
    private String[] args;

    public static void main(String args[]) {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            System.err.println("Starting Client");
            String name = "Server";
            Registry registry = LocateRegistry.getRegistry(null);
            ServerCommands server = (ServerCommands) registry.lookup(name);
            
            String command = GetArg(args, 0);
            
            switch (command.trim()) {
                case "upload":
                    break;
                case "download":
                    break;
                case "dir":
                    String relativePath = GetArg(args, 1);
                    String dirStr = server.dir(relativePath);
                    System.err.println("Directory of " + relativePath + ": ");
                    System.out.println(dirStr);
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
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
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

}
