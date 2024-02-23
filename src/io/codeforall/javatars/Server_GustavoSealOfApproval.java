package io.codeforall.javatars;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server_GustavoSealOfApproval {
    public static void main(String[] args) {

        //Declare server socket and initialize it with null
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            //Create a server socket, bound to port 8080
            serverSocket = new ServerSocket(8080);
            System.out.println("Server running at " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());

            //Initiate While loop to prevent the server process from stopping (downside: only accepts one connection at a time)
            while (true){

                //Declares and initializes client Socket with a new connection from server Socket which is waiting
                clientSocket = serverSocket.accept();
                System.out.println("New client accepted: " + clientSocket.getLocalAddress().getHostAddress() + ":" + clientSocket.getLocalPort());

                //Declare BufferedReader to handle input from the clientSocket and OutputStream to handle output to the clientSocket
                // and initializes them with null
                BufferedReader in = null;
                OutputStream out = null;

                try {
                    //Creates the streams to/from our clientSocket
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    //out = clientSocket.getOutputStream();
                    // ^ Using the direct OutputStream that we get from the clientSocket
                    // is a possible choice given it does what we want but if we're writing small amounts of data frequently
                    // each write() call could result in a separate I/O operation, which can be inefficient
                    out = new BufferedOutputStream(clientSocket.getOutputStream());

                    //Read the request header from the client
                    String clientRequest = in.readLine(); // Verb (GET) + Path to Resource + Protocol Used (HTTP/1.0 or HTTP/1.1)
                    System.out.println(clientRequest + "\n");

                    String verb = clientRequest.split(" ")[0];
                    String pathToResource = "webServerResources" + clientRequest.split(" ")[1];

                    if(pathToResource.equals("webServerResources/")) {
                        pathToResource = "webServerResources/index.html";
                    }

                    //Check if file doesn't exist
                    pathToResource = resourceExists(pathToResource);

                    if(verb.equals("GET")){ // our response headers are only if we receive a GET request
                        if(pathToResource.isEmpty()){
                            out.write("HTTP/1.0 404 Not Found\r\n".getBytes());
                            out.write("Content-Type: text/html; charset=UTF-8\r\n\r\n".getBytes());
                            out.write("<html><body><h1>404 Not Found</h1></body></html>".getBytes());
                            out.flush();
                        }

                        else {
                            // Writes our response header and resource content to the output stream of our client Socket
                            out.write(getHeader(pathToResource).getBytes());
                            out.write(getContent(pathToResource));
                            out.flush();
                        }
                    }
                }
                catch (Exception e) {
                    System.err.println("Error handling client: " + e.getMessage());
                    e.printStackTrace();
                }

                finally {
                    // Ensure resources are always closed
                    if (in != null) {
                        try { in.close(); } catch (Exception ignored) {  }
                    }
                    if (out != null) {
                        try { out.close(); } catch (Exception ignored) {  }
                    }
                    if (!clientSocket.isClosed()) {
                        try { clientSocket.close(); } catch (Exception ignored) {  }
                    }
                }
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

    private static String getHeader(String pathToResource){

        File file = new File(pathToResource);
        // Get size of file in bytes
        long fileSize = file.length();
        // Get type of file by using the extension in the path String
        String fileType = pathToResource.substring(pathToResource.lastIndexOf(".")+1).toLowerCase();

        return switch (fileType) {
            case "png", "ico" -> "HTTP/1.0 200 Document Follows\r\n" +
                    "Content-Type: image/" + fileType + "\r\n" +
                    "Content-Length: " + fileSize + "\r\n\r\n";
            case "html" -> "HTTP/1.0 200 Document Follows\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + fileSize + "\r\n\r\n";
            default -> "HTTP/1.0 404 Not Found\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + fileSize + " \r\n" +
                    "\r\n";
        };

    }

    private static byte[] getContent(String pathToResource){
        FileInputStream reader;
        BufferedInputStream bReader;
        byte[] resource = null;
        try {
            reader = new FileInputStream(pathToResource);
            bReader = new BufferedInputStream(reader);
            resource = bReader.readAllBytes();

        } catch (FileNotFoundException e) {
            try {
                reader = new FileInputStream("webServerResources/404.html");
                bReader = new BufferedInputStream(reader);
                resource = bReader.readAllBytes();

            } catch (FileNotFoundException ex) {
                System.err.println("Failed to load the 404 error page content.");

            } catch (IOException ex) {
                System.err.println("IOException error: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("IOException error: " + e.getMessage());
        }

        return resource;
    }

    private static String resourceExists(String pathToResource){
        File file = new File(pathToResource);
        if(file.exists()){
            return pathToResource;
        }
        if (pathToResource.equals("webServerResources/404.html")){
            return "";
        }
        return resourceExists("webServerResources/404.html");
    }

}
