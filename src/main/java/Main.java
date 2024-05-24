import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

//     Uncomment this block to pass the first stage

       ServerSocket serverSocket = null; //listens for incoming requests
       Socket clientSocket = null;  //handles communication between the client and server

     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

       //send a response to the client after successful connection with OutputStream
       clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
     } catch (IOException e) {
       System.out.println("connection unsuccessful: " + e.getMessage());
     }
  }
}
