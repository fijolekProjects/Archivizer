package file;

import java.awt.BorderLayout;
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
import javax.swing.table.AbstractTableModel;

public class MainGuiClass extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JButton addFile;
    private JButton removeFile;
    private JFileChooser fileChooser;
    private final JTable table;
    private static MyTableModel tableModel;
    private String currentFilename;
    private static List<String> checksumList = new ArrayList<String>();
    private static JFrame frame;
    private static FileServer server = new FileServer();
    private static TestClient client = new TestClient();
    private static List<String[]> allRowsData = new ArrayList<String[]>();

    private String tempFileName;
    private String[] tempData;

    public MainGuiClass() {
	super(new BorderLayout());
	table = new JTable(new MyTableModel());
	tableModel = (MyTableModel) table.getModel();
	JScrollPane scrollPane = new JScrollPane(table);

	add(scrollPane);
	fileChooser = new JFileChooser();
	fileChooser.setMultiSelectionEnabled(true);

	addFile = new JButton("Choose file...");
	addFile.addActionListener(this);
	add(addFile, BorderLayout.AFTER_LINE_ENDS);

	removeFile = new JButton("Remove file");
	removeFile.addActionListener(this);
	add(removeFile, BorderLayout.AFTER_LAST_LINE);
    };

    class MyTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] columnNames = { "Filename", "Archivize Date", "Filesize", "MD5 checksum",
		"Filepath" };
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

	public String getColumnName(int col) {
	    return columnNames[col];
	}

	public Class<? extends Object> getColumnClass(int c) {
	    return getValueAt(0, c).getClass();
	}

	public void setValueAt(Object value, int row, int col) {
	    ((Vector<Object>) data.get(row)).setElementAt(value, col);
	    fireTableCellUpdated(row, col);
	}

	public boolean isCellEditable(int row, int col) {
	    if (columnNames.length == col) {
		return true;
	    } else {
		return false;
	    }
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
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		try {
		    saveProgramState(allRowsData);
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		System.exit(0);
	    }
	});
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	MainGuiClass newContentPane = new MainGuiClass();
	frame.setContentPane(newContentPane);
	// Display the window.
	frame.setSize(1000, 600);
	try {
	    loadProgramState();
	} catch (IOException e2) {
	    e2.printStackTrace();
	}
	// frame.pack();
	frame.setVisible(true);
    }

    private static void loadProgramState() throws IOException {
	FileInputStream fileInProgram = new FileInputStream("programSavedState.dat");
	ObjectInputStream objInProgram = new ObjectInputStream(fileInProgram);
	try {
	    @SuppressWarnings("unchecked")
	    ArrayList<String[]> objProgram = (ArrayList<String[]>) objInProgram.readObject();
	    for (int i = 0; i < objProgram.size(); i++) {
		allRowsData.add(objProgram.get(i));
		tableModel.insertData(allRowsData.get(i));
	    }
	    objInProgram.close();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

	FileInputStream fileInChecksum = new FileInputStream("checksumSavedState.dat");
	ObjectInputStream objInChecksum = new ObjectInputStream(fileInChecksum);
	try {
	    @SuppressWarnings("unchecked")
	    ArrayList<String> objChecksum = (ArrayList<String>) objInChecksum.readObject();
	    for (int i = 0; i < objChecksum.size(); i++) {
		checksumList.add(objChecksum.get(i));
	    }
	    objInChecksum.close();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

    }

    private static void saveProgramState(List<String[]> allDataList) throws IOException {
	FileOutputStream fileOutProgram = new FileOutputStream("programSavedState.dat");
	ObjectOutputStream objOutProgram = new ObjectOutputStream(fileOutProgram);
	objOutProgram.writeObject(allRowsData);
	objOutProgram.close();

	FileOutputStream fileOutChecksum = new FileOutputStream("checksumSavedState.dat");
	ObjectOutputStream objOutChecksum = new ObjectOutputStream(fileOutChecksum);
	objOutChecksum.writeObject(checksumList);
	objOutChecksum.close();
    }

    public static void main(String[] args) throws Exception {

	server.startServer();

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	});
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == addFile) {
	    addFileButtonAction(e);
	}
	if (e.getSource() == removeFile) {
	    removeFileButtonAction(e);
	}

    }

    private void addFileButtonAction(ActionEvent e) {
	int returnVal = fileChooser.showOpenDialog(MainGuiClass.this);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final File file[] = fileChooser.getSelectedFiles();

	    Thread sendingFilesThread = new Thread(new Runnable() {

		@Override
		public void run() {
		    for (int i = 0; i < file.length; i++) {

			try {
			    if (!checksumList.contains(computeMD5(file[i]))) {

				tempFileName = file[i].getAbsoluteFile().toString();

				TestClient.setFileName(tempFileName);

				try {
				    client.startClient();
				} catch (Exception e1) {
				    e1.printStackTrace();
				}

				tempData = prepareRowData(file[i]);
				allRowsData.add(tempData);
				tableModel.insertData(tempData);

			    } else {
				currentFilename = file[i].getName();
				JOptionPane.showMessageDialog(frame, "File: " + currentFilename
					+ " has been already archivized! Try different file.",
					"Warning!", JOptionPane.WARNING_MESSAGE);
			    }
			} catch (NoSuchAlgorithmException | IOException e2) {
			    e2.printStackTrace();
			}
		    }

		}
	    });
	    sendingFilesThread.start();
	}
    }

    private void removeFileButtonAction(ActionEvent e) {
	try {
	    int[] rowsSelected = table.getSelectedRows();

	    for (int i = 0; i < rowsSelected.length; i++) {
		tableModel.removeRow(rowsSelected[i] - i);
		checksumList.remove(rowsSelected[i] - i);
		allRowsData.remove(rowsSelected[i] - i);
	    }

	} catch (ArrayIndexOutOfBoundsException ex) {
	    // Nothing happens, only to preserve printing exception to console
	}
    }

    private String[] prepareRowData(File file) {
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
}