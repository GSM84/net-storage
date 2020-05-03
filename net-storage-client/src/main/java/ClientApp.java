import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ClientApp {
/*
    private static final int BUFFER_SIZE = 1024;
    private static final int PORT        = 8189;

    private static String[] messages =
            {"The best way to predict the future is to create it.",
                    "As you think, so shall you become.",
                    "The noblest pleasure is the joy of understanding.",
                    "Courage is grace under pressure.",
                    "*exit*"};

    public static void main(String[] args) {

        System.out.println("Starting MySelectorClientExample...");
        try {
            InetAddress       hostIP    = InetAddress.getLocalHost();
            InetSocketAddress myAddress = new InetSocketAddress(hostIP, PORT);
            SocketChannel     myClient  = SocketChannel.open(myAddress);

            System.out.println(String.format("Trying to connect to %s:%d...", myAddress.getHostName(), myAddress.getPort()));

            RandomAccessFile src = new RandomAccessFile("C:\\GITHUB\\Lections\\Algo\\Lesson1\\[веб] Методичка 1. Алгоритмы и структуры данных на Java. Базовый курс.docx", "r");
            FileChannel srcChannel = src.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(50);
            int readedButch = srcChannel.read(buffer);
            while (readedButch > -1) {
                buffer.flip();

                myClient.write(buffer);
                buffer.clear();
                readedButch = srcChannel.read(buffer);
            }


//            for (String msg: messages) {
//                ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
//                myBuffer.put(msg.getBytes());
//                myBuffer.flip();
//                int bytesWritten = myClient.write(myBuffer);
//                System.out.println(String.format("Sending Message...: %s\nbytesWritten...: %d",msg, bytesWritten));
//            }
            System.out.println("Closing Client connection...");
            myClient.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }*/

}
