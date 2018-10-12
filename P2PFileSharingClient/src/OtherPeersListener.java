import java.net.*;
import java.util.*;

public class OtherPeersListener implements Runnable  {

    private String ipaddress;
    private Integer port;
    private ServerSocket serverSocket;

    OtherPeersListener() throws Exception {
        serverSocket = new ServerSocket(0);
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if(i.isSiteLocalAddress()) {
                        ipaddress = i.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        port = serverSocket.getLocalPort();
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                Socket anotherClientSock = serverSocket.accept();
                AnotherClientConn anotherClient = new AnotherClientConn(anotherClientSock);
                (new Thread(anotherClient)).start();
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public String getIPaddress() {
        return ipaddress;
    }

    public Integer getPort() {
        return port;
    }

}
