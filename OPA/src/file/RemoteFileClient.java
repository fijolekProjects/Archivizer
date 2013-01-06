package file;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteFileClient extends Remote {

    public String getFromClientSentFilename() throws RemoteException;

    public void getFileFromServer() throws Exception;

    public void sendFileFromClient() throws Exception;
}
