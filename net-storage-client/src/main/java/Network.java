import java.io.*;
import java.net.Socket;


public class Network {
    private static Socket           socket;
    private static DataOutputStream out;
    private static DataInputStream  in;

    public static void start(byte[] _srvIPAddress, int _port)  {
        try {
            System.out.println("Establishing connection to server...");
            socket = new Socket("localhost", _port);

            out    = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in     = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            System.out.println("Connected.");

        } catch (IOException e) {
            System.err.println("Server unavaliable.");
        }
    }

    public static DataOutputStream getOutputStream() {
        return out;
    }

    public static DataInputStream getInputStream() {
        return in;
    }

    static void writeSignalByte(byte _signalByte){
        try {
            out.writeByte(_signalByte);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeShorMessage(String _msg){
        try {
            byte[] msgByte = _msg.getBytes("UTF-8");
            out.writeInt(msgByte.length);
            out.write(msgByte);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean authorizeUser(String _login, String _password){
        writeSignalByte((byte) 14);
        writeShorMessage(_login);
        writeShorMessage(_password);

        return true;
    }

    static void stop(){
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
