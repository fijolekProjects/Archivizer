package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;

/**
 * Example client which sends a file (given on the command line) to the example
 * server using a RemoteInputStream.
 * 
 * @author James Ahlborn
 */
public class TestClient {
    private static String fromClientSentFilename;

    public static String getFromClientSentFilename() {
	return fromClientSentFilename;
    }

    public static void setFromClientSentFilename(String fileName) {
	TestClient.fromClientSentFilename = fileName;
    }

    public void sendFileFromClient() throws Exception {
	// grab the file name from the commandline

	// get a handle to the remote service to which we want to send the file
	Registry registry = LocateRegistry.getRegistry();
	RemoteFileServer stub = (RemoteFileServer) registry.lookup("RemoteFileServer");

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
	    stub.sendFile(istream.export());

	} finally {
	    // always make a best attempt to shutdown RemoteInputStream
	    istream.close();
	}
	System.out.println("Finished sending file " + fromClientSentFilename);

    }

    public void getFileFromServer() throws Exception {
	Registry registry = LocateRegistry.getRegistry();
	RemoteFileServer stub = (RemoteFileServer) registry.lookup("RemoteFileServer");
	System.out.println("Sending file from server");
	String tDir = System.getProperty("java.io.tmpdir");
	SimpleRemoteOutputStream ostream;
	ostream = new SimpleRemoteOutputStream(new FileOutputStream(tDir + File.separator
		+ "fromServer" + FileServer.getFromServerGotFilename()));
	try {
	    // call the remote method on the server. the server will actually
	    // interact with the RMI "server" we started above to retrieve the
	    // file data
	    stub.getFile(ostream.export());

	} finally {
	    // always make a best attempt to shutdown RemoteInputStream
	    ostream.close();
	}
	System.out.println("Finished sending file " + FileServer.getFromServerGotFilename());

    }

}