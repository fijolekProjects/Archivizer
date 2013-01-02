package file;
import java.io.IOException;
import java.rmi.Remote;

import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * A simple Remote interface for an RMI server which consumes a
 * RemoteInputStream.
 * 
 * @author James Ahlborn
 */  
public interface RemoteFileServer extends Remote
{
	public void sendFile(RemoteInputStream ristream) throws IOException;
} 