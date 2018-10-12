/*
	in order to test the project on one machine, the pair of IP address and port number serves as identifier of user
	therefore, when peer wants to change the score of another peer, it sends “SCORE of ”+IP Addr of B+":"+port of B+“ : n” 		to FT instead of “SCORE of ”+IP Addr of B+“ : 1” to FT
*/

import java.net.*;
import java.util.*;

public class FileTracker {
    public static void main(String argc[]) throws Exception {
        ServerSocket server = new ServerSocket(6789);
        Hashtable<IP_port_pair, Requests_Uploads_pair> peersList = new Hashtable<>();
        Hashtable<String, List<FileInfo>> filesList = new Hashtable<>();
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket clientSocket = server.accept();
            ClientThread client = new ClientThread(clientSocket, peersList, filesList);
            (new Thread(client)).start();
        }
    }
}
