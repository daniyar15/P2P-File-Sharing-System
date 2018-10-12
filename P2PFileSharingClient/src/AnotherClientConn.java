import java.io.*;
import java.net.*;
import java.util.Random;

public class AnotherClientConn implements Runnable {

    private Socket anotherClient;
    private BufferedReader in;
    private PrintWriter out;

    AnotherClientConn(Socket anotherClient) throws Exception {
        this.anotherClient = anotherClient;
        in = new BufferedReader(new InputStreamReader(anotherClient.getInputStream()));
        out = new PrintWriter(anotherClient.getOutputStream());
    }

    public void run() {
        try {
            String line = in.readLine();
            if(line.startsWith("DOWNLOAD: ")) {
                String filenameToGet = parseFileName(line);
                String typeToGet = parseType(line);
                String sizeToGet = parseSize(line);

                File sharedFolder = new File("shared");
                File[] sharedFiles = sharedFolder.listFiles();

                for(File item: sharedFiles) {
                    String[] names = item.getName().split("\\.");
                    String filename = names[0];
                    String type = names[1];
                    String size = item.length() + " bytes";
                    if(filenameToGet.equals(filename) && typeToGet.equals(type) && sizeToGet.equals(size)) {
                        Random rand = new Random();
                        int n = rand.nextInt(100) + 1;
                        if (n < 50) {
                            out.write("FILE:\n");
                            out.flush();

                            //receive file
                            InputStream inputStream = new FileInputStream(item);
                            OutputStream outputStream = anotherClient.getOutputStream();
                            byte[] bytes = new byte[16*1024];

                            int count;
                            while ((count = inputStream.read(bytes)) > 0) {
                                outputStream.write(bytes, 0, count);
                            }

                            outputStream.close();
                            inputStream.close();
                        } else {
                            out.write("NO!\n");
                            out.flush();
                        }
                        break;
                    }
                }
            } else {
                out.write("wrong request\n");
                out.flush();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String parseFileName(String line) {
        int spaceIndex = line.indexOf(" ");
        int commaIndex = line.indexOf(",");
        return line.substring(spaceIndex + 1, commaIndex);
    }
    
    private String parseType(String line) {
        int firstCommaIndex = line.indexOf(",");
        int secondCommaIndex = line.indexOf(",", firstCommaIndex + 1);
        return line.substring(firstCommaIndex + 2, secondCommaIndex);
    }

    private String parseSize(String line) {
        int firstCommaIndex = line.indexOf(",");
        int secondCommaIndex = line.indexOf(",", firstCommaIndex + 1);
        return line.substring(secondCommaIndex + 2);
    }
}
