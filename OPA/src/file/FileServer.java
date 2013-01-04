package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteOutputStream;
import com.healthmarketscience.rmiio.RemoteOutputStreamClient;

/**
 * Simple example server which can be the target of a streamed file.
 * 
 * @author James Ahlborn
 */

public class FileServer implements RemoteFileServer {
    private boolean sendingComplete;
    private static String fromServerGotFilename;

    @Override
    public void getFile(RemoteOutputStream rostream) throws IOException {
	String tDir = System.getProperty("java.io.tmpdir");
	OutputStream ostream = RemoteOutputStreamClient.wrap(rostream);
	FileInputStream istream = new FileInputStream(tDir + File.separator + fromServerGotFilename);
	try {
	    byte[] buf = new byte[1024];	
	    int bytesRead = 0;
	    while ((bytesRead = istream.read(buf)) >= 0) {
		ostream.write(buf, 0, bytesRead);
	    }
	    ostream.flush();
	} finally {

	    try {
		if (istream != null) {
		    istream.close();

		}
	    } finally {
		if (ostream != null) {
		    ostream.close();
		}
	    }
	    
	}
	
    }
    
    public void sendFile(RemoteInputStream ristream) throws IOException {
	InputStream istream = RemoteInputStreamClient.wrap(ristream);
	FileOutputStream ostream = null;

	try {
	    sendingComplete = false;
	    String fn = TestClient.getFromClientSentFilename();
	    int lastIndexOfSlash = fn.lastIndexOf("\\");
	    String fileNameString = fn.substring(lastIndexOfSlash + 1);
	    System.out.println(fileNameString);
	    String tDir = System.getProperty("java.io.tmpdir");

	    ostream = new FileOutputStream(tDir + File.separator + fileNameString);
	    System.out.println("Writing file " + fileNameString);
	    byte[] buf = new byte[1024];

	    int bytesRead = 0;
	    while ((bytesRead = istream.read(buf)) >= 0) {
		ostream.write(buf, 0, bytesRead);
	    }

	    ostream.flush();

	    System.out.println("Finished writing file " + fileNameString);
	    sendingComplete = true;
	} finally {

	    try {
		if (istream != null) {
		    istream.close();

		}
	    } finally {
		if (ostream != null) {
		    ostream.close();
		}
	    }
	}
    }

    public boolean getsendingComplete() {
	return sendingComplete;
    }

    public void startServer() throws Exception {

	FileServer server = new FileServer();
	RemoteFileServer stub = (RemoteFileServer) UnicastRemoteObject.exportObject(server, 0);

	// bind to registry
	Registry registry = LocateRegistry.getRegistry();
	registry.bind("RemoteFileServer", stub);

	System.out.println("Server ready");

    }

    public static String getFromServerGotFilename() {
	return fromServerGotFilename;
    }

    public static void setFromServerGotFilename(String fromServerGotFilename) {
	FileServer.fromServerGotFilename = fromServerGotFilename;
    }

    

}