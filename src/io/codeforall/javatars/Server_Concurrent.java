package io.codeforall.javatars;

import java.net.ServerSocket;
import java.net.Socket;

public class Server_Concurrent {
    public static void main(String[] args) {

        //Declare server socket and initialize it with null
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            //Create a server socket, bound to port 8080
            serverSocket = new ServerSocket(8080);
            System.out.println("Server running at " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());

            //Initiate While loop to prevent the server process from stopping
            while (true){

                clientSocket = serverSocket.accept();
                System.out.println("New client accepted: " + clientSocket.getRemoteSocketAddress());
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();

            }
        }

        catch (Exception e) {
            System.err.println("Could not create server Socket: " + e.getMessage());
        }

        finally {
            // Close the server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try { serverSocket.close(); } catch (Exception ignored) { }
            }
        }
    }

}
