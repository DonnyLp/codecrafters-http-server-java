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
                char [] requestCharacters = new char[512];
                bufferedReader.read(requestCharacters); //pass the buffered inputStream into a char array
                String requestString = new String(requestCharacters);
                System.out.println(requestString);

                String [] requestSplit = requestString.split("\r\n",0);
                System.out.println(requestSplit[3]);

//                // Part 3: Validate the request target's url
//                if (requestSplit[1].equals("/")){
//                    clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
//                }else{
//                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
//                }

//                //Part 4: Send a response with a body
//
//                String body = "";
//                System.out.println(body);
//                //return 404 if echo keyword doesn't exist
//                if(!requestSplit[1].contains("echo") && !requestSplit[1].equals("/")){
//                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
//                }else{
//                    //Set the body if the request isn't an empty request
//                    if(!requestSplit[1].equals("/")){
//                        body = requestSplit[1].substring(6);
//                    }
//
//                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
//                            + body.length() +"\r\n\r\n"
//                            + body).getBytes());
//                }

                //Part 5: User Agent

                String userAgentEndpoint = requestSplit[0];
                String userAgentHeaderValue = "";

                //grab and return the corresponding user agent header value in the response
                if(userAgentEndpoint.contains("user-agent")){
                    userAgentHeaderValue = requestSplit[3].substring(11).trim();
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgentHeaderValue.length() +"\r\n\r\n" + userAgentHeaderValue).getBytes());
                }else{
                    clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes()
);
                }

            } catch (IOException e) {
                System.out.println("connection unsuccessful: " + e.getMessage());
            }
    }
}
