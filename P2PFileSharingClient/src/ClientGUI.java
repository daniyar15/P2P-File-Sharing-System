import java.net.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ClientGUI extends JFrame implements ActionListener {
    private OtherPeersListener conn;
    private Socket FT;
    private BufferedReader inFromFT;
    private PrintWriter outToFT;

    private JButton search;  //Buttons
    private JButton dload;
    private JButton close;

    private JList jl;   // List that will show found files
    private JTextField tf, tf2; // Two textfields: one is for typing a file name, the other is just to show the selected file
    private DefaultListModel<String> listModel; // Used to select items in the list of found files


    ClientGUI(OtherPeersListener conn) {
        super("Example GUI");

        this.conn = conn;
        connectToFT();

        setLayout(null);
        setSize(500, 600);

        JLabel label; //Label "File Name
        label = new JLabel("File name:");
        label.setBounds(50, 50, 80, 20);
        add(label);

        tf = new JTextField();
        tf.setBounds(130, 50, 220, 20);
        add(tf);

        search = new JButton("Search");
        search.setBounds(360, 50, 80, 20);
        search.addActionListener(this);
        add(search);

        listModel = new DefaultListModel<>();
        jl = new JList<>(listModel);

        JScrollPane listScroller = new JScrollPane(jl);
        listScroller.setBounds(50, 80, 300, 300);

        add(listScroller);

        dload = new JButton("Download");
        dload.setBounds(200, 400, 130, 20);
        dload.addActionListener(this);
        add(dload);

        tf2 = new JTextField();
        tf2.setBounds(200, 430, 130, 20);
        add(tf2);

        close = new JButton("Close");
        close.setBounds(360, 470, 80, 20);
        close.addActionListener(this);
        add(close);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == search) { //If search button is pressed show 25 randomly generated file info in text area
            String fileName = tf.getText();
            outToFT.write("SEARCH: " + fileName + "\n");
            outToFT.flush();
            try {
                int i = 0;
                listModel.removeAllElements();
                while(true) {
                    String oneLine = inFromFT.readLine();
                    if(oneLine.isEmpty()) {
                        break;
                    }
                    if(oneLine.equals("FOUND:") || oneLine.equals("NOT FOUND")) {
                        continue;
                    }
                    listModel.add(i, oneLine);
                    i++;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } else if (e.getSource() == dload) {   //If download button is pressed get the selected value from the list and show it in text field
            downloadFile(jl.getSelectedValue().toString());
        } else if (e.getSource() == close) { //If close button is pressed exit
            outToFT.write("BYE\n");
            outToFT.flush();
            System.exit(0);
        }
    }

    private void connectToFT() {
        try {
            FT = new Socket("10.101.37.173", 6789);
            inFromFT = new BufferedReader(new InputStreamReader(FT.getInputStream()));
            outToFT = new PrintWriter(FT.getOutputStream());
            outToFT.write("HELLO\n");
            outToFT.flush();
            String line = inFromFT.readLine();
            if(line.equals("HI")) {
                uploadFiles(inFromFT, outToFT);
            } else {
                System.exit(0);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void uploadFiles(BufferedReader inFromFT, PrintWriter outToFT) {
        File sharedFolder = new File("shared2");
        if(!sharedFolder.exists()) {
            boolean SharedFolderExists = false;
            try {
                sharedFolder.mkdir();
                SharedFolderExists = true;
            } catch(SecurityException se) {
                System.out.println(se.getMessage());
            }
            if (SharedFolderExists) {
                System.out.println("Directory Shared created");
            }
        }

        File downloadsFolder = new File("downloads");
        if (!downloadsFolder.exists()) {
            boolean downloadFolderExists = false;
            try {
                downloadsFolder.mkdir();
                downloadFolderExists = true;
            } catch(SecurityException se) {
                System.out.println(se.getMessage());
            }
            if(downloadFolderExists) {
                System.out.println("Directory downloads created");
            }
        }

        File[] sharedFiles = sharedFolder.listFiles();
        if(sharedFiles.length == 0 || sharedFiles.length > 5) {
            System.exit(-1);
        }
        for(File item: sharedFiles) {
            String record = "";
            String[] names = item.getName().split("\\.");
            record += names[0];
            record += ", ";
            record += names[1];
            record += ", ";
            record += item.length();
            record += " bytes, ";

            long lm = item.lastModified();
            SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yy");
            String lastModified = dateformat.format(lm);
            record += lastModified;
            record += ", ";

            record += conn.getIPaddress();
            record += ", ";
            record += conn.getPort();
            record += "\n";

            outToFT.write(record);
            outToFT.flush();

        }
        outToFT.write("\n");
        outToFT.flush();
        try {
            String succ = inFromFT.readLine();
            System.out.println(succ);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void downloadFile(String record) {
        try {
            String ipaddressToConnect = parseIPaddress(record);
            Integer portToConnect = parsePort(record);
            Socket peerSocket = new Socket(ipaddressToConnect, portToConnect);
            BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            PrintWriter outToPeer = new PrintWriter(peerSocket.getOutputStream());
            String filename = parseFileName(record);
            String type = parseType(record);
            String size = parseSize(record);
            outToPeer.write("DOWNLOAD: " + filename + ", " + type + ", " + size + "\n");
            outToPeer.flush();
            String peerAnswer = inFromPeer.readLine();
            if(peerAnswer.equals("FILE:")) {
                outToFT.write("SCORE of " + ipaddressToConnect + ":" + portToConnect + " : 1\n");
                outToFT.flush();

                File toWrite = new File("downloads", filename + "." + type);
                toWrite.createNewFile();
                byte[] bytes = new byte[16 * 1024];
                InputStream inputStream = peerSocket.getInputStream();
                OutputStream outputStream = new FileOutputStream(toWrite);
                int count;
                while ((count = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, count);
                }

                tf2.setText(filename + "downloaded");
            } else if (peerAnswer.equals("NO!")) {
                outToFT.write("SCORE of " + ipaddressToConnect + ":" + portToConnect + " : 0\n");
                outToFT.flush();
                tf2.setText(filename + "was not downloaded");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String parseIPaddress(String record) {
        int indexIPaddress = record.indexOf("IP address: ");
        indexIPaddress += 12;
        int indexComma = record.indexOf(",", indexIPaddress);
        return record.substring(indexIPaddress, indexComma);
    }

    private Integer parsePort(String record) {
        int indexPort = record.indexOf("port number: ");
        indexPort += 13;
        int indexComma = record.indexOf(",", indexPort);
        return Integer.valueOf(record.substring(indexPort, indexComma));
    }

    private String parseType(String record) {
        int indexType = record.indexOf("type: ");
        indexType += 6;
        int indexComma = record.indexOf(",", indexType);
        return record.substring(indexType, indexComma);
    }

    private String parseSize(String record) {
        int indexSize = record.indexOf("size: ");
        indexSize += 6;
        int indexComma = record.indexOf(",", indexSize);
        return record.substring(indexSize, indexComma);
    }

    private String parseFileName(String record) {
        int indexComma = record.indexOf(",");
        return record.substring(0, indexComma);
    }
}
