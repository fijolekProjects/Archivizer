package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;

/**
 * Example client which sends a file (given on the command line) to the example
 * server using a RemoteInputStream.
 * 
 * @author James Ahlborn
 */
public class TestClient implements RemoteClient{
    private static String fromClientSentFilename;
    private RemoteFileServer stubServer;
    private Registry registry;
    
    @Override
    public String getFromClientSentFilename() throws RemoteException {
	return fromClientSentFilename;
    }

    public static void setFromClientSentFilename(String fileName) {
	TestClient.fromClientSentFilename = fileName;
    }
    
    public void startClient(String hostName, int portNumber) throws Exception {
	TestClient client = new TestClient();
	RemoteClient stubClient = (RemoteClient) UnicastRemoteObject.exportObject(client, 0);
	
	// bind to registry
	registry = LocateRegistry.getRegistry(hostName, portNumber);
	registry.bind("RemoteClient", stubClient);
    }
    @Override
    public void sendFileFromClient() throws Exception {
	// grab the file name from the commandline

	// get a handle to the remote service to which we want to send the file	
	
	
	System.out.println("Sending file from client" + fromClientSentFilename);

	// setup the remote input stream. note, the client here is actually
	// acting as an RMI server (very confusing, i know). this code sets up
	// an
	// RMI server in the client, which the RemoteFileServer will then
	// interact with to get the file data.
	SimpleRemoteInputStream istream = new SimpleRemoteInputStream(new FileInputStream(
		fromClientSentFilename));
	try {
	    // call the remote method on the server. the server will actually
	    // interact with the RMI "server" we started above to retrieve the
	    // file data
	    createStubServer();
	    stubServer.sendFileToServer(istream.export());

	} finally {
	    // always make a best attempt to shutdown RemoteInputStream
	    istream.close();
	}
	System.out.println("Finished sending file " + fromClientSentFilename);

    }
    @Override
    public void getFileFromServer() throws Exception {
	createStubServer();
	System.out.println("Sending file from server");
	String tempDirectory = System.getProperty("java.io.tmpdir");
	SimpleRemoteOutputStream ostream;

	ostream = new SimpleRemoteOutputStream(new FileOutputStream(tempDirectory + File.separator
		+ "clientSide__" + stubServer.getFromServerGotFilename()));
	try {
	    // call the remote method on the server. the server will actually
	    // interact with the RMI "server" we started above to retrieve the
	    // file data
	    stubServer.writeFileToClient(ostream.export());

	} finally {
	    // always make a best attempt to shutdown RemoteInputStream
	    ostream.close();
	}
	System.out.println("Finished sending file " + stubServer.getFromServerGotFilename());
    }

    public void removeFileFromClient(String filename) {
	String tempDirectory = System.getProperty("java.io.tmpdir");
	File fileToRemove = new File(tempDirectory + "clientSide__" + filename);
	fileToRemove.delete();
    }
    
    private void createStubServer() {
	try {
//	    Registry registry = LocateRegistry.getRegistry();
	    stubServer = (RemoteFileServer) registry.lookup("RemoteFileServer");
	} catch (RemoteException | NotBoundException e) {
	    e.printStackTrace();
	}
    }
}