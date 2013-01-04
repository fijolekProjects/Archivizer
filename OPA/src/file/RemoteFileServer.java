package file;
import java.io.IOException;
import java.rmi.Remote;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;

/**
 * A simple Remote interface for an RMI server which consumes a
 * RemoteInputStream.
 * 
 * @author James Ahlborn
 */  
public interface RemoteFileServer extends Remote
{
	public void sendFile(RemoteInputStream ristream) throws IOException;
	
	public void getFile(RemoteOutputStream rostream) throws IOException;
} 