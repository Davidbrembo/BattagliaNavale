package server.main;

import server.network.ServerSocketManager;

public class ServerMain {
    public static void main(String[] args) {
        ServerSocketManager server = new ServerSocketManager(); // porta a tua scelta
        server.start(12345);
    }
}