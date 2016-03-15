package mit.spbau.ru;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 14.03.16.
 */

public class FtpClient {
    private final String host;
    private final int port;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public FtpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
//            System.err.println("Connecting to the server");
            socket = new Socket(host, port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
//            System.err.println("Connection established");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnect() {
        try {
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> executeList(String path) {
//        System.err.println("executing list");
        List<String> result;
        try {
            outputStream.writeInt(1);
            outputStream.writeUTF(path);
            result = new ArrayList<>();
            int filesCount = inputStream.readInt();
            for (int i = 0; i < filesCount; i++) {
                result.add(inputStream.readUTF() + " " + inputStream.readUTF());
            }
//            System.err.println("done");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ByteArrayOutputStream executeGet(String path) {
        ByteArrayOutputStream result = null;
        try {
            outputStream.writeInt(2);
            outputStream.writeUTF(path);
            result = new ByteArrayOutputStream();
            long packetsCount = inputStream.readLong();
            byte[] data = new byte[FtpServer.BUFFER_SIZE];
            for (int i = 0; i < packetsCount; i++) {
                int len = inputStream.read(data);
                result.write(data, 0, len);
            }
            result.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
