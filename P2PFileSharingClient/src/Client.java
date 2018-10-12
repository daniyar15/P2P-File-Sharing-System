import javax.swing.JFrame;

public class Client{
    public static void main(String[]args){
        try {
            OtherPeersListener conn = new OtherPeersListener();
            (new Thread(conn)).start();
            ClientGUI clientgui = new ClientGUI(conn);
            clientgui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the window if x button is pressed
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}