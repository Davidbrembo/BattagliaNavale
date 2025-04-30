package server.main;

import server.network.ServerSocketManager;

public class ServerMain {
    public static void main(String[] args) {
        ServerSocketManager server = new ServerSocketManager(12345); // porta a tua scelta
        server.start();
    }
}