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

                //Grab the request
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String request = bufferedReader.readLine();
                System.out.println(request);

                String [] requestSplit = request.split(" ",0);
                System.out.println(requestSplit[1]);

//                // Part 3: Validate the request target's url
//                if (requestSplit[1].equals("/")){
//                    clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
//                }else{
//                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
//                }

                //Part 4: Send a response with a body

                String body = "";
                System.out.println(body);

                //return 404 if echo keyword doesn't exist
                if(!requestSplit[1].contains("echo")){
                    clientSocket.getOutputStream().write("HTTP/1.1 400 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
                }else{
                    //Grab the string after the echo keyword
                    body = requestSplit[1].substring(6);
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                            + body.length() +"\r\n\r\n"
                            + body).getBytes());
                }

            } catch (IOException e) {
                System.out.println("connection unsuccessful: " + e.getMessage());
            }
    }
}
