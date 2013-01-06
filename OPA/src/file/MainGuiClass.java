package file;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class MainGuiClass extends JPanel implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JButton sendFileToServer;
    private JButton getFileFromServer;
    private JButton removeFile;
    private JButton openFile;
    private JFileChooser fileChooser;
    private final JTable table;

    private RemoteFileServer stubServer;

    private static MyTableModel tableModel;
    private static JFrame frame;
    private static FileServer server = new FileServer();
    private static FileClient client = new FileClient();
    private static List<String[]> allRowsDataList = new ArrayList<String[]>();
    private static List<String> checksumList = new ArrayList<String>();
    private static List<String> allFilenameList = new ArrayList<String>();
    private static List<String> filesGotFromServerList = new ArrayList<String>();

    private String tempFromClientSentFilename;
    private String[] tempData;

    private static String hostName;
    private static int portNumber;

    public static void setHostName(String hostName) {
	MainGuiClass.hostName = hostName;
    }

    public static void setPortNumber(int portNumber) {
	MainGuiClass.portNumber = portNumber;
    }

    public MainGuiClass() {
	super(new BorderLayout());
	table = new JTable(new MyTableModel());
	tableModel = (MyTableModel) table.getModel();
	JScrollPane scrollPane = new JScrollPane(table);

	add(scrollPane);
	fileChooser = new JFileChooser();
	fileChooser.setMultiSelectionEnabled(true);

	sendFileToServer = new JButton("Add file to server");
	sendFileToServer.addActionListener(this);
	add(sendFileToServer, BorderLayout.EAST);

	removeFile = new JButton("Remove file");
	removeFile.addActionListener(this);
	add(removeFile, BorderLayout.SOUTH);

	getFileFromServer = new JButton("Get file from server");
	getFileFromServer.addActionListener(this);
	add(getFileFromServer, BorderLayout.WEST);

	openFile = new JButton("Open file");
	openFile.addActionListener(this);
	add(openFile, BorderLayout.NORTH);

    };

    class MyTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] columnNames = { "Filename", "Archivize Date", "Filesize", "MD5 checksum",
		"Original Filepath" };
	private Vector<Vector<Object>> data = new Vector<Vector<Object>>();

	@Override
	public int getColumnCount() {
	    return columnNames.length;

	}

	@Override
	public int getRowCount() {
	    return data.size();

	}

	@Override
	public Object getValueAt(int row, int col) {
	    return ((Vector<?>) data.get(row)).get(col);
	}

	@Override
	public String getColumnName(int col) {
	    return columnNames[col];
	}

	public void insertData(String[] values) {
	    data.add(new Vector<Object>());
	    for (int i = 0; i < values.length; i++) {
		((Vector<Object>) data.get(data.size() - 1)).add(values[i]);
	    }
	    fireTableDataChanged();
	}

	public void removeRow(int row) {
	    data.removeElementAt(row);
	    fireTableDataChanged();
	}
    }

    private static void createAndShowGUI() {

	frame = new JFrame("Archivizer");

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	MainGuiClass newContentPane = new MainGuiClass();
	frame.setContentPane(newContentPane);
	frame.setSize(1000, 600);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);

	loadProgramStateOnBeginning();
	saveProgramStateOnExit();

    }

    private static void loadProgramStateOnBeginning() {
	try {
	    loadProgramState();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void loadProgramState() throws IOException {
	loadListState("allRowsDataListState.dat", allRowsDataList);
	for (int i = 0; i < allRowsDataList.size(); i++) {
	    tableModel.insertData(allRowsDataList.get(i));
	}
	loadListState("checksumListState.dat", checksumList);
	loadListState("allFilenamesListState.dat", allFilenameList);
	loadListState("filesGotFromServerListState.dat", filesGotFromServerList);

    }

    @SuppressWarnings("unchecked")
    private static <T> void loadListState(String listStateFilename, List<T> listToLoad)
	    throws IOException {
	FileInputStream fileInProgram = new FileInputStream(listStateFilename);
	ObjectInputStream objInProgram = new ObjectInputStream(fileInProgram);
	try {
	    ArrayList<T> objProgram = (ArrayList<T>) objInProgram.readObject();
	    for (int i = 0; i < objProgram.size(); i++) {
		listToLoad.add((T) objProgram.get(i));
	    }
	    objInProgram.close();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

    }

    private static void saveProgramStateOnExit() {
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		try {
		    saveProgramState();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		System.exit(0);
	    }
	});
    }

    private static void saveProgramState() throws IOException {
	saveListState("allRowsDataListState.dat", allRowsDataList);
	saveListState("checksumListState.dat", checksumList);
	saveListState("allFilenamesListState.dat", allFilenameList);
	saveListState("filesGotFromServerListState.dat", filesGotFromServerList);
    }

    private static <T> void saveListState(String listStateFilename, List<T> listToSave)
	    throws IOException {
	FileOutputStream fileOutProgram;
	fileOutProgram = new FileOutputStream(listStateFilename);
	ObjectOutputStream objOutProgram = new ObjectOutputStream(fileOutProgram);
	objOutProgram.writeObject(listToSave);
	objOutProgram.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == sendFileToServer) {
	    sendFileToServerButtonAction(e);
	}
	if (e.getSource() == removeFile) {
	    try {
		removeFileButtonAction(e);
	    } catch (RemoteException e1) {
		e1.printStackTrace();
	    }
	}
	if (e.getSource() == getFileFromServer) {
	    getFileFromServerButtonAction(e);
	}
	if (e.getSource() == openFile) {
	    openFileButtonAction(e);
	}

    }

    private void openFileButtonAction(ActionEvent e) {
	Desktop desktop = Desktop.getDesktop();
	int rowSelected = table.getSelectedRow();
	String filenameToOpen = allFilenameList.get(rowSelected);

	if (filesGotFromServerList.contains(filenameToOpen)) {
	    String tempDirectory = System.getProperty("java.io.tmpdir");
	    File fileToOpen = new File(tempDirectory + File.separator + "clientSide__"
		    + filenameToOpen);
	    try {
		desktop.open(fileToOpen);
	    } catch (IOException e1) {
		e1.printStackTrace();
	    }
	} else {
	    JOptionPane.showMessageDialog(frame, "File: " + filenameToOpen
		    + " has not been got from server! Get file from server first", "Warning!",
		    JOptionPane.WARNING_MESSAGE);
	}
    }

    private void sendFileToServerButtonAction(ActionEvent e) {
	int returnVal = fileChooser.showOpenDialog(MainGuiClass.this);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final File file[] = fileChooser.getSelectedFiles();
	    Thread sendFilesFromClientThread = new Thread(new Runnable() {

		@Override
		public void run() {
		    for (int i = 0; i < file.length; i++) {
			try {
			    if (!checksumList.contains(computeMD5(file[i]))) {
				String currentFilename;
				currentFilename = file[i].getName();

				tempFromClientSentFilename = file[i].getAbsoluteFile().toString();
				FileClient.setFromClientSentFilename(tempFromClientSentFilename);

				if (allFilenameList.contains(currentFilename)) {
				    int rowToRemove = allFilenameList.indexOf(currentFilename);
				    removeCurrentFile(rowToRemove);
				}
				// w removeCurrentFile usuwam nazwe pliku z
				// allFilenameList,
				// wiec 'nadpisuje' to miejsce na liscie
				allFilenameList.add(currentFilename);
				try {
				    client.sendFileFromClient();
				} catch (Exception e) {
				    e.printStackTrace();
				}

				tempData = prepareRowData(file[i]);
				allRowsDataList.add(tempData);
				tableModel.insertData(tempData);

			    } else {

				JOptionPane.showMessageDialog(frame, "File: " + file[i].getName()
					+ " has been already archivized! Try different file.",
					"Warning!", JOptionPane.WARNING_MESSAGE);
			    }
			} catch (NoSuchAlgorithmException | IOException e) {
			    e.printStackTrace();
			}
		    }
		}
	    });
	    sendFilesFromClientThread.start();
	}
    }

    private void getFileFromServerButtonAction(ActionEvent e) {

	Thread getFilesFromServerThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		int[] rowsSelected = table.getSelectedRows();
		for (int i = 0; i < rowsSelected.length; i++) {

		    String currentFileToGetFromServer = allFilenameList.get(rowsSelected[i]);
		    createStubServer();
		    try {
			stubServer.setFromServerGotFilename(currentFileToGetFromServer);
		    } catch (RemoteException e) {
			e.printStackTrace();
		    }

		    if (!filesGotFromServerList.contains(currentFileToGetFromServer)) {
			filesGotFromServerList.add(currentFileToGetFromServer);
		    }
		    try {
			client.getFileFromServer();
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }

		}
	    }
	});
	getFilesFromServerThread.start();
    }

    private void removeFileButtonAction(ActionEvent e) throws RemoteException {
	int[] rowsSelected = table.getSelectedRows();
	for (int i = 0; i < rowsSelected.length; i++) {
	    removeCurrentFile(rowsSelected[i] - i);
	}

    }

    private void removeCurrentFile(int row) {
	String currentFileToRemove = allFilenameList.get(row);
	// remove file from all lists
	tableModel.removeRow(row);
	checksumList.remove(row);
	allRowsDataList.remove(row);
	if (!filesGotFromServerList.isEmpty()) {
	    if (filesGotFromServerList.contains(currentFileToRemove)) {
		filesGotFromServerList.remove(currentFileToRemove);
	    }
	}
	allFilenameList.remove(row);
	// remove real file form server
	createStubServer();
	try {
	    stubServer.removeFileFromServer(currentFileToRemove);
	} catch (RemoteException e) {
	    e.printStackTrace();
	}
	// TODO zostawic?
	// remove real file from client
	// client.removeFileFromClient(currentFileToRemove);

    }

    private void createStubServer() {
	try {
	    Registry registry = LocateRegistry.getRegistry(hostName, portNumber);
	    stubServer = (RemoteFileServer) registry.lookup("RemoteFileServer");
	} catch (RemoteException | NotBoundException e) {
	    e.printStackTrace();
	}
    }

    private String[] prepareRowData(File file) {
	// TODO FILEPATH
	String fileName = file.getName();
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	Calendar cal = Calendar.getInstance();
	String archivizeDate = dateFormat.format(cal.getTime()).toString();
	Float fileSizeInKB = (float) Math.round((file.length() / 1024));
	String MD5checksum = null;
	String filePath = file.getAbsolutePath();
	try {
	    MD5checksum = computeMD5(file);
	    checksumList.add(MD5checksum);
	} catch (NoSuchAlgorithmException | IOException e) {
	    e.printStackTrace();
	}

	String values[] = { fileName, archivizeDate, fileSizeInKB.toString() + " KB", MD5checksum,
		filePath };

	return values;
    }

    private String computeMD5(File file) throws NoSuchAlgorithmException, IOException {
	MessageDigest md = MessageDigest.getInstance("MD5");
	FileInputStream fis = new FileInputStream(file);
	byte[] dataBytes = new byte[1024];

	int nread = 0;

	while ((nread = fis.read(dataBytes)) != -1) {
	    md.update(dataBytes, 0, nread);
	}

	byte[] mdbytes = md.digest();
	fis.close();
	// convert the byte to hex format
	StringBuffer sb = new StringBuffer("");
	for (int i = 0; i < mdbytes.length; i++) {
	    sb.append(Integer.toString((mdbytes[i] & 0xFF), 16));
	}

	return sb.toString();
    }

    public static void start() throws Exception {
	server.startServer(portNumber);
	client.startClient(hostName, portNumber);

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	});

    }
}