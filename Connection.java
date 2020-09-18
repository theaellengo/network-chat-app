import java.io.*;
import java.net.*;
import java.time.*;

public class Connection extends Thread {

    Socket endpoint;
    Server server;
    String name;
    DataInputStream reader;
    DataOutputStream writer;

    public Connection(Socket endpoint, Server server, String name) {
        this.endpoint = endpoint;
        this.server = server;
        this.name = name; //client associated with connection
    }

    //sends string to server to client i
    public void sendMsgToClient(String sender, String msg) {
        try {
            writer.writeUTF(sender);
            writer.writeUTF(msg);
        } catch (Exception e) {
            //e.printStackTrace();
            //uncomment to not show up in console
            //shows up when a client disconnects
        }
    }

    public void sendToAll(String sender, String msg) {
        for (int i = 0; i < server.connections.size(); i++) {
            Connection c = server.connections.get(i);
            if (!endpoint.getRemoteSocketAddress().equals(server.connections.get(i).endpoint.getRemoteSocketAddress())) {
                System.out.println("[" + LocalTime.now() + "] Client " + endpoint.getRemoteSocketAddress() + 
                    " sent a message to " + server.connections.get(i).endpoint.getRemoteSocketAddress());
                server.addLogs("[" + LocalTime.now() + "] Client " + endpoint.getRemoteSocketAddress() + 
                    " sent a message to " + server.connections.get(i).endpoint.getRemoteSocketAddress());
            }
            c.sendMsgToClient(sender, msg);
        }
    }

    public void close() {
        try {
            reader.close();
            writer.close();
            endpoint.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            reader = new DataInputStream(endpoint.getInputStream());
            writer = new DataOutputStream(endpoint.getOutputStream());
            
            while (true) {
                try {
                    sendToAll(name, reader.readUTF());
                } catch (Exception e) {
                    break;
                }
            }

            sendToAll("Server", name + " has left the chat.");
            System.out.println("Server: Client at " + endpoint.getRemoteSocketAddress() + " has disconnected.");
            server.addLogs("Server: Client at " + endpoint.getRemoteSocketAddress() + " has disconnected.");
            server.connections.remove(this);

            //closes server if no clients left
            if (server.connections.isEmpty()) {
                server.askToDownload();
                server.closeserver();
            }

            close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}