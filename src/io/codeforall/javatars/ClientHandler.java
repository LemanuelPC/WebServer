package io.codeforall.javatars;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{

    Socket clientSocket = null;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void start() throws IOException {

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
            if(clientRequest == null){
                clientRequest = "GET / HTTP/1.0";
            }

            String verb = clientRequest.split(" ")[0];
            String pathToResource = "webServerResources" + clientRequest.split(" ")[1];

            if (pathToResource.equals("webServerResources/")) {
                pathToResource = "webServerResources/index.html";
            }

            //Check if file doesn't exist
            pathToResource = resourceExists(pathToResource);

            if (verb.equals("GET")) { // our response headers are only if we receive a GET request
                if (pathToResource.isEmpty()) {
                    out.write("HTTP/1.0 404 Not Found\r\n".getBytes());
                    out.write("Content-Type: text/html; charset=UTF-8\r\n\r\n".getBytes());
                    out.write("<html><body><h1>404 Not Found</h1></body></html>".getBytes());
                    out.flush();
                } else {
                    // Writes our response header and resource content to the output stream of our client Socket
                    out.write(getHeader(pathToResource).getBytes());
                    out.write(getContent(pathToResource));
                    out.flush();
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure resources are always closed
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {
                }
            }
            if (!clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
