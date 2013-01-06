package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteOutputStream;
import com.healthmarketscience.rmiio.RemoteOutputStreamClient;

public class FileServer implements RemoteFileServer {
    private static String fromServerGotFilename;
    private String tempDirectory = System.getProperty("java.io.tmpdir");

    public void startServer() throws Exception {

	FileServer server = new FileServer();
	RemoteFileServer stubServer = (RemoteFileServer) UnicastRemoteObject
		.exportObject(server, 0);

	// bind to registry
	Registry registry = LocateRegistry.getRegistry();
	registry.bind("RemoteFileServer", stubServer);

	System.out.println("Server ready");

    }

    @Override
    public void setFromServerGotFilename(String fromServerGotFilename) throws RemoteException {
	FileServer.fromServerGotFilename = fromServerGotFilename;

    }

    @Override
    public String getFromServerGotFilename() {
	return fromServerGotFilename;
    }

    @Override
    public void sendFileToServer(RemoteInputStream ristream) throws IOException {
	InputStream istream = RemoteInputStreamClient.wrap(ristream);
	FileOutputStream ostream = null;
	String fn = null;
	try {
	    Registry registry = LocateRegistry.getRegistry();
	    RemoteClient stubClient;
	    try {
		stubClient = (RemoteClient) registry.lookup("RemoteClient");
		fn = stubClient.getFromClientSentFilename();
	    } catch (NotBoundException e) {
		e.printStackTrace();
	    }

	    int lastIndexOfSlash = fn.lastIndexOf("\\");
	    String fileNameString = fn.substring(lastIndexOfSlash + 1);
	    System.out.println(fileNameString);


	    ostream = new FileOutputStream(tempDirectory + File.separator + "serverSide__" + fileNameString);
	    System.out.println("Writing file " + fileNameString);
	    byte[] buf = new byte[1024];

	    int bytesRead = 0;
	    while ((bytesRead = istream.read(buf)) >= 0) {
		ostream.write(buf, 0, bytesRead);
	    }

	    ostream.flush();

	    System.out.println("Finished writing file " + fileNameString);
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

    @Override
    public void writeFileToClient(RemoteOutputStream rostream) throws IOException {
	OutputStream ostream = RemoteOutputStreamClient.wrap(rostream);
	FileInputStream istream = new FileInputStream(tempDirectory + File.separator + "serverSide__"
		+ fromServerGotFilename);
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

    @Override
    public void removeFileFromServer(String filename) throws RemoteException {
	File fileToRemove = new File(tempDirectory + "serverSide__" + filename);
	fileToRemove.delete();
    }

}