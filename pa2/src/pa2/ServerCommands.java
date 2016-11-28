package pa2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerCommands extends Remote {
    String dir(String relativePath) throws RemoteException;   
       
}
