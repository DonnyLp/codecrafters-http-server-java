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
                InputStream inputStream = clientSocket.getInputStream(); //returns a stream of bytes
                
                //Pass in an InputStreamReader which converts the inputstream bytes to characters
                //BufferedReader then bufferes those characters to be readily accessed
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); 

                char [] requestCharacters = new char[512];
                bufferedReader.read(requestCharacters); //pass the buffered inputStream into a char array
                String requestString = new String(requestCharacters);
            
                String [] requestSplit = requestString.split("\r\n",0); //Split the request by sections: Request Line, Headers, Body
                String [] requestLine = requestSplit[0].split(" ", 0); //Split the contents of the first section: Request Line
                String path = requestLine[1]; //this holds the path of the request
                
                boolean echoExists = path.contains("/echo");
                boolean userAgentExists = path.equals("/user-agent");
                boolean emptyPathExists = path.equals("/");


                //Grab the instance of the user agent header
                int agentUserIndex = requestString.indexOf("User-Agent:");
                
                String echoString = ""; //holds the string the proceeds the echo keyword

               // Part 3: Validate the request target's url: handle an empty path

                //Part 4: Send a response with a body

                //Part 5: User Agent

                String userAgentEndpoint = path;
                String userAgentHeaderValue = "";

                //grab and return the corresponding user agent header value in the response

               if (emptyPathExists){
                   clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                   clientSocket.close();
                   serverSocket.close();
                }else if(echoExists){
                    echoString = path.substring(6);
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                           + echoString.length() +"\r\n\r\n"
                           + echoString).getBytes());
                    clientSocket.close();
                    serverSocket.close();
                }else if(userAgentExists){
                    userAgentHeaderValue = requestString.substring(agentUserIndex + 11).trim();
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgentHeaderValue.length() +"\r\n\r\n" + userAgentHeaderValue).getBytes());
                    clientSocket.close();
                    serverSocket.close();
                }else{
                    clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
                    clientSocket.close();
                    serverSocket.close();
                }
 
            } catch (IOException e) {
                System.out.println("connection unsuccessful: " + e.getMessage());
            }
    }
}