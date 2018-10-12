import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ClientThread implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private Hashtable<IP_port_pair, Requests_Uploads_pair> peersList;
    private Hashtable<String, List<FileInfo>> filesList;
    private String ipAddress;
    private Integer port;
    private List<String> filenamesList;

    ClientThread(Socket client, Hashtable<IP_port_pair, Requests_Uploads_pair> peersList, Hashtable<String, List<FileInfo>> filesList)
            throws Exception {
        this.client = client;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream());
        this.peersList = peersList;
        this.filesList = filesList;
        filenamesList = new ArrayList<>();
    }

    public void run() {
        try {
            String line = in.readLine();
            if(line.equals("HELLO")) {
                out.write("HI\n");
                out.flush();
            } else {
                out.write("REJECTED\n");
                out.flush();
                out.close();
                in.close();
                client.close();
                return;
            }

            boolean endFileRecords;
            for(int i = 0; i < 5; i++) {
                line = in.readLine();
                endFileRecords = parseFileRecord(line);
                if(i == 0 && endFileRecords) {
                    out.close();
                    in.close();
                    client.close();
                    return;
                }
                if(endFileRecords) break;
            }

            IP_port_pair ip_port_pair = new IP_port_pair(ipAddress, port);
            Requests_Uploads_pair requests_uploads_pair = new Requests_Uploads_pair();
            peersList.put(ip_port_pair, requests_uploads_pair);

            out.write("reading was successful\n");
            out.flush();

            while(true) {
                line = in.readLine();
                if(line.startsWith("SEARCH: ")) {
                    parseSearch(line);
                } else if(line.startsWith("SCORE of ")) {
                    parseScoreChange(line);
                } else if (line.equals("BYE")) {
                    parseBye();
                    break;
                } else {
                    out.write("incorrect request\n");
                    out.flush();
                }

            }

            out.close();
            in.close();
            client.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private boolean parseFileRecord(String record) {

        if(record.isEmpty()) return true;

        String[] items = record.split(", ");

        String name = items[0];
        filenamesList.add(name);

        String type = items[1];
        String size = items[2];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        LocalDate lastmodified = LocalDate.parse(items[3], formatter);

        ipAddress = items[4];
        port = Integer.valueOf(items[5]);

        FileInfo file_record = new FileInfo(type, size, lastmodified, ipAddress, port);
        List<FileInfo> list_for_filename = filesList.get(name);
        if(list_for_filename != null) {
            list_for_filename.add(file_record);
        } else {
            list_for_filename = new ArrayList<>();
            list_for_filename.add(file_record);
            filesList.put(name, list_for_filename);
        }

        return false;
    }

    private void parseSearch(String line) {
        String requestedFile = line.substring(8);
        List<FileInfo> requestedList = filesList.get(requestedFile);
        if (requestedList != null && requestedList.size() != 0) {
            out.write("FOUND:\n");
            for(FileInfo item: requestedList) {
                String requestedIP = item.getIpaddress();
                Integer requestedPort = item.getPort();
                int requestedScore = peersList.get(new IP_port_pair(requestedIP, requestedPort)).getScore();
                out.write(requestedFile + ", " + item + ", score: "
                        + requestedScore + "\n");
            }
            out.write("\n");
            out.flush();
        } else {
            out.write("NOT FOUND\n");
            out.write("\n");
            out.flush();
        }
    }

    private void parseScoreChange(String line) {
        int index_1st_colon = line.indexOf(':');
        int index_2nd_colon = line.indexOf(':', index_1st_colon + 1);
        String ipaddressB = line.substring(9, index_1st_colon); // getting string till the first colon
        // getting string from the colon till the first space after beginning of the ip address
        Integer portB = Integer.valueOf(line.substring(index_1st_colon + 1, line.indexOf(' ', 9)));
        Integer score_to_increment = Integer.valueOf(line.substring(index_2nd_colon + 2));
        IP_port_pair peer_to_change = new IP_port_pair(ipaddressB, portB);
        Requests_Uploads_pair record_to_change = peersList.get(peer_to_change);
        record_to_change.incrementRequests();
        if(score_to_increment == 1) {
            record_to_change.incrementUploads();
        }

    }

    private void parseBye() {
        for(String indiv_filename: filenamesList) {
            List<FileInfo> with_file_to_delete = filesList.get(indiv_filename);
            for(FileInfo item: with_file_to_delete) {
                if (item.getIpaddress().equals(ipAddress) && item.getPort().equals(port)) {
                    with_file_to_delete.remove(item);
                    out.write("Successful delete of file\n");
                    out.flush();
                    break;
                }
            }
        }
    }
}
