package nl.gerwint;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Client implements Runnable {

    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private final List<Listener> listeners = new ArrayList<>();


    public boolean connect(InetAddress address, int port) {
        try {
            socket = new Socket(address, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread c1 = new Thread(this);
            c1.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {

        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean sendCommand(String command) {
        try {
            if (command != null) {
                out.write(command);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public abstract String commandReceived(String command);

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                for (Listener l : listeners) {
                    l.messageReceived(commandReceived(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Connection Closed");
            close();
        }
    }
}
