package io.codeforall.javatars;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

// Full Disclaimer: Only to be seen by people who believe final results are what truly matter

public class Server {

    public static void main(String[] args) {
        FileReader reader = null;

            try {
                ServerSocket socket = new ServerSocket(8080);
                Socket clientSocket = null;

                while(true) {

                    if(clientSocket == null || clientSocket.isClosed()) {
                        clientSocket = socket.accept();
                        System.out.println("New client accepted: " + clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort());
                    }

                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String messageReceived = in.readLine();
                    System.out.println(messageReceived);
                    String[] fileRequested = messageReceived.split(" ");

                    if (fileRequested[0].equals("GET")) {

                        if (fileRequested[1].equals("/") || fileRequested[1].equals("/index.html")) {
                            out.println(getHeader("html", read("/index.html").length) + new String(read("/index.html")));
                        }

                        else if (fileRequested[1].equals("/logo.png")) {
                            BufferedOutputStream outImg = new BufferedOutputStream(clientSocket.getOutputStream());
                            out.println(getHeader("png", read(fileRequested[1]).length));
                            System.out.println(getHeader("png", read(fileRequested[1]).length));
                            outImg.write(read("/logo.png"));
                            outImg.flush();
                            outImg.close();
                        }

                        else if(fileRequested[1].equals("/favicon.ico")) {
                            BufferedOutputStream outImg = new BufferedOutputStream(clientSocket.getOutputStream());
                            out.println(getHeader("ico", read(fileRequested[1]).length));
                            System.out.println(getHeader("ico", read(fileRequested[1]).length));
                            outImg.write(read("/favicon.ico"));
                            outImg.flush();
                            outImg.close();

                        }

                        else {
                            out.println(getHeader("", read("/404.html").length) + new String(read("/404.html")));
                        }

                    }

                    out.close();
                    in.close();
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

    }

    private static byte[] read(String fileName){
        BufferedInputStream bReader;
        try {
            FileInputStream reader = new FileInputStream("webServerResources" + fileName);
            bReader = new BufferedInputStream( reader);
            return bReader.readAllBytes();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static String getHeader(String fileType, int size){
        if(fileType.equals("html")) {
            return  "HTTP/1.0 200 Document Follows\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + size + " \r\n" +
                    "\r\n";
        }
        if(fileType.equals("png")) {
            return "HTTP/1.0 200 Document Follows\r\n" +
                    "Content-Type: image/png \r\n" +
                    "Content-Length: " + size + " \r\n";
        }
        if(fileType.equals("ico")){
            return "HTTP/1.0 200 Document Follows\r\n" +
                    "Content-Type: image/ico \r\n" +
                    "Content-Length: " + size + " \r\n";
        }
            return  "HTTP/1.0 404 Not Found\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + size + " \r\n" +
                    "\r\n";

    }
}
