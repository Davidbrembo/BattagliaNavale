package server.main;

import java.io.IOException;

import server.network.ServerSocketManager;

public class ServerMain {
    public static void main(String[] args) {
        ServerSocketManager server = null;
		try {
			server = new ServerSocketManager(12345);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        server.start();
    }
}