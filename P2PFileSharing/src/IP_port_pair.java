public class IP_port_pair {

    private final String ip;
    private final Integer port;

    IP_port_pair(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public int hashCode() {
        return ip.hashCode() ^ port.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IP_port_pair) && ((IP_port_pair) obj).ip.equals(ip)
                && ((IP_port_pair) obj).port.equals(port);
    }

    @Override
    public String toString() {
        return "IP address: " + ip + ", port: " + port;
    }
}
