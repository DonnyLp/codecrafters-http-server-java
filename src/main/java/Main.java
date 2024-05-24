import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                System.out.println("listening for requests from the client");

                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = bufferedReader.readLine();
                System.out.println(line);

                String [] requestSplit = line.split(" ",0);

                //validate the request target's url
                if (requestSplit[1] == "/"){
                    clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                }else{
                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }

            } catch (IOException e) {
                System.out.println("connection unsuccessful: " + e.getMessage());
            }
    }
}
