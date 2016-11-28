package pa2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface ServerCommands extends Remote {
    public String dir(String relativePath) throws RemoteException;   
    public boolean mkdir(String relativePath) throws RemoteException;   
    public boolean rmdir(String relativePath) throws RemoteException;    
    public boolean rm(String relativePath) throws RemoteException;    
    public boolean shutdown() throws RemoteException;    
    public long getFileSize(String relativePath) throws RemoteException; 
    public ArrayList<Byte> getFileBytes(String relativePath, int offset, int bytesize) throws RemoteException; 
}
