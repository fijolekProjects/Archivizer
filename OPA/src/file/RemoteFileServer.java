package file;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;

public interface RemoteFileServer extends Remote {
    public void sendFileToServer(RemoteInputStream ristream) throws IOException;

    public void writeFileToClient(RemoteOutputStream rostream) throws IOException;

    public String getFromServerGotFilename() throws RemoteException;

    public void setFromServerGotFilename(String fromServerGotFilename) throws RemoteException;
    
    public void removeFileFromServer(String filename) throws RemoteException;
}