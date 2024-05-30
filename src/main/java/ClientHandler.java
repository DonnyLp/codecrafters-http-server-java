import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private String directoryPath;

    // enum that defines different avaliable operations served to the client
    private enum serverOperation{           
        ECHO,
        USER_AGENT,
        EMPTY,
        FILE,
        INVALID_REQUEST,
    };

    /*
     * Constructs a ClientHander instance with a given client socket
     * @param clientSocket - clientsocket instance received after the server accepts the client's request
     */
    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public ClientHandler(Socket clientSocket, String directoryPath){
        this.clientSocket = clientSocket;
        this.directoryPath = directoryPath;
    }

    /*
     * 
     */
    @Override
    public void run() {

        try{
            handleRequest(clientSocket);
            System.out.print("");
        }catch(IOException e){
            e.printStackTrace();
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
                String requestType = requestLine[0]; 
                String requestURI = requestLine[1]; 
                
                String echoString; 
                String userAgentHeaderValue;
 
                //Grab the index of where the user agent header is found in the request string
                int agentUserIndex = requestString.indexOf("User-Agent:");
                

                //All operations supported for responses
                switch (defineServerOperation(requestURI)) {
                    case ECHO ->{
                        echoString = requestURI.substring(6);
                        clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                               + echoString.length() +"\r\n\r\n"
                               + echoString).getBytes());
                    }
                    case USER_AGENT->{
                        //Grab the user agent header information and return its contents to the client
                        userAgentHeaderValue = requestString.substring(agentUserIndex + 11).trim();
                        clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgentHeaderValue.length() +"\r\n\r\n" + userAgentHeaderValue).getBytes());
                    }
                    case EMPTY->{
                        clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    }
                    case FILE ->{
                        handleGetFileRequest(directoryPath, clientSocket, requestString,requestType);
                    }
                    case INVALID_REQUEST->{
                        clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
                    }
                    default -> throw new HttpServerException("Server operation not assigned properly");
                }   
                clientSocket.close();
    }



    private void handleGetFileRequest(String directoryPath, Socket clientSocket, String requestURI, String requestType) throws FileNotFoundException, IOException{
        
        //Construct the newly wanted absolute path for the given filename
        
        String fileName = requestURI.split("/")[2];
        directoryPath += fileName;

        //Create a directory instance
        Path directory = Path.of(directoryPath);
        char [] fileBuffer = new char[1024];
        FileReader fileReader;

        boolean fileExists = Files.exists(directory);
            
            if(fileExists){

                //Jump to the appropriate request type 
                switch (requestType) {
                    case "GET" -> {
                    //Create a new fileReader instance for the existing file
                    fileReader = new FileReader(directoryPath);
                    fileReader.read(fileBuffer);
                    String fileContents = new String(fileBuffer).trim();
                
                    //Respond with content length, cotent type: application/octet-stream, and body as the files contents
                    clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length:" +
                    fileContents.length() + "\r\n\r\n" + fileContents + "\r\n").getBytes());
                    fileReader.close();

                    }
                    case "POST" -> {
                        //Create a new filewriter instance to write to the given filename
                    }
                    default -> throw new HttpServerException("Request Type" + requestType + " is currently not supported");
                };

            }else{
                clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
            } 
        }

     /*
      * helper that validates which operation is to be executed by the server
      * @param path The path present in the request line
      * @return serverOperation An enum constant is provided depending on the given path
     */

    private serverOperation defineServerOperation(String requestURI){
        serverOperation operation;

        if(requestURI.contains("/echo")) operation = serverOperation.ECHO;
        else if(requestURI.equals("/user-agent")) operation = serverOperation.USER_AGENT;
        else if(requestURI.equals("/")) operation = serverOperation.EMPTY;
        else if (requestURI.contains("/files/")) operation = serverOperation.FILE;
        else operation = serverOperation.INVALID_REQUEST;
        return operation;
    }

    private String [] getRequestLine(String requestString){ 
        String [] requestSplit = requestString.split("\r\n",0); 
        return requestSplit[0].split(" ", 0);
    }

    private String getRequestURI(String requestString){
        String requestUri = getRequestURI(requestString);
        return requestUri;
    }

}