import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    
    //attributes for the port and the thread pool initializer 
    private Socket clientSocket;
    
    // enum that defines different avaliable operations served to the client
    private enum  serverOperation{           
        ECHO,
        USER_AGENT,
        EMPTY,
        INVALID_REQUEST,
    };

    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket ;
    }

    /*
     * Implicitly called when passed into 
     */
    @Override
    public void run() {
        try{
            handleRequest(clientSocket);
        }catch(IOException e){
            e.printStackTrace();
        }finally{
        }
    }
    
    public void handleRequest(Socket clientSocket)throws IOException{

                //Grab the request from the clientSocket inputStream
                InputStream inputStream = clientSocket.getInputStream(); //returns a stream of bytes
                
                //Pass in an InputStreamReader which converts the inputstream bytes to characters
                //BufferedReader then bufferes those characters to be readily accessed
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); 

                //Conversion of the character buffer to a String
                char [] requestCharacters = new char[512];
                bufferedReader.read(requestCharacters); //pass the buffered inputStream into a char array
                String requestString = new String(requestCharacters);

                //Split the request string by sections: Request Line, Headers, Body
                //Further spliting used for parsing parts of each request section i.e path
                String [] requestSplit = requestString.split("\r\n",0); 
                String [] requestLine = requestSplit[0].split(" ", 0); 
                String path = requestLine[1]; 
                
                String echoString = ""; 
                String userAgentHeaderValue = "";
 
                //Grab the index of where the user agent header is found in the request string
                int agentUserIndex = requestString.indexOf("User-Agent:");

                //All operations supported for responses
                switch (checkOperationType(path)) {
                    case ECHO ->{
                        echoString = path.substring(6);
                        clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                               + echoString.length() +"\r\n\r\n"
                               + echoString).getBytes());
                    }
                    case USER_AGENT->{
                        userAgentHeaderValue = requestString.substring(agentUserIndex + 11).trim();
                        clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgentHeaderValue.length() +"\r\n\r\n" + userAgentHeaderValue).getBytes());
                    }
                    case EMPTY->{
                        clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    }
                    case INVALID_REQUEST->{
                        clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
                    }
                    default -> throw new HttpServerException("Server operation not assigned properly");
                }   
                clientSocket.close();
    }

     /*
     helper that validates which operation is to be done by the server
    @param path The path present in the request line
    @return serverOperation An enum constant is provided depending on the given path
     */

    private static serverOperation checkOperationType(String path){
        serverOperation operation;

        if(path.contains("echo")) operation = serverOperation.ECHO;
        else if(path.equals("/user-agent")) operation = serverOperation.USER_AGENT;
        else if(path.equals("/")) operation = serverOperation.EMPTY;
        else operation = serverOperation.INVALID_REQUEST;
        return operation;

    }
}