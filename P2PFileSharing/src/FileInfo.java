import java.time.LocalDate;

public class FileInfo {

    private final String type;
    private final String size;
    private final LocalDate lastmodified;
    private final String ipaddress;
    private final Integer port;

    FileInfo(String type, String size, LocalDate lastmodified, String ipaddress, Integer port) {
        this.type = type;
        this.size = size;
        this.lastmodified = lastmodified;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    public String getIpaddress() { return  ipaddress; }

    public Integer getPort() { return port; }

    @Override
    public String toString() {
        return "type: " + type + ", size: " + size + ", last modified date: " + lastmodified +
                ", IP address: " + ipaddress + ", port number: " + port;
    }

}
