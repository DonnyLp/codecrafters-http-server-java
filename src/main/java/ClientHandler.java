import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private String directoryPath;

    // enum that defines different avaliable operations served to the client
    private enum serverOperation {
        ECHO,
        USER_AGENT, // operation: serve user agent info
        EMPTY,
        FILE,
        INVALID_REQUEST,
    };

    /**
     * Constructs a ClientHander instance with a given client socket
     * 
     * @param clientSocket - clientsocket instance received after the server accepts
     *                     the client's request
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Constructs a ClientHander instance with a given client socket and directory
     * 
     * @param clientSocket  Clientsocket instance received after the server accepts
     *                      the client's request
     * @param directoryPath The path to the directory where it will be either
     *                      created or updated.
     */

    public ClientHandler(Socket clientSocket, String directoryPath) {
        this.clientSocket = clientSocket;
        this.directoryPath = directoryPath;
    }

    /**
     * Executes the logic for handling client requests.
     * This method is called when the thread starts running.
     */
    @Override
    public void run() {

        try {
            handleRequest(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the incoming HTTP request from the client.
     *
     * @param clientSocket The socket connected to the client.
     * @throws IOException If an I/O error occurs while handling the request.
     */
    public void handleRequest(Socket clientSocket) throws IOException {

        // Grab the request from the clientSocket inputStream
        InputStream inputStream = clientSocket.getInputStream(); // returns a stream of bytes

        // Pass in an InputStreamReader which converts the inputstream bytes to
        // characters
        // BufferedReader then bufferes those characters to be readily accessed
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // Conversion of the character buffer to a String
        char[] requestCharacters = new char[512];
        bufferedReader.read(requestCharacters); // pass the buffered inputStream into a char array
        String requestString = new String(requestCharacters);
        String requestURI = getRequestURI(requestString);

        // Jump to a supported server operation
        switch (defineServerOperation(requestURI)) {
            case ECHO -> {
                handleEchoRequest(clientSocket, requestURI, requestString);
            }
            case USER_AGENT -> {
                int agentUserIndex = requestString.indexOf("User-Agent");
                // Grab the user agent header information and return its contents to the client
                String userAgentHeaderValue = requestString.substring(agentUserIndex + 11).trim();
                clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:"
                        + userAgentHeaderValue.length() + "\r\n\r\n" + userAgentHeaderValue).getBytes());
            }
            case EMPTY -> {
                clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            }
            case FILE -> {
                handleFileRequest(directoryPath, clientSocket, requestString);
            }
            case INVALID_REQUEST -> {
                clientSocket.getOutputStream().write(
                        "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
            }
            default -> throw new HttpServerException("Server operation not assigned properly");
        }
    }

    private void handleEchoRequest(Socket clientSocket, String requestURI, String requestString) throws IOException {

        int encodingIndex = requestString.indexOf("Accept-Encoding");
        String requestBody = requestURI.substring(6);
        String[] encodingTypes = requestString.substring(encodingIndex + 16).trim().split(",", 0);

        boolean isValidType = false;

        for (String type : encodingTypes) {
            if (type.contains("gzip"))
                isValidType = true;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(requestBody.getBytes("UTF-8"));
        }
        byte[] gzipData = byteArrayOutputStream.toByteArray();
        if (!isValidType) {
            clientSocket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                    +requestBody.length() + "\r\n\r\n"
                    + requestBody).getBytes());
        } else {
            String httpResponse = "HTTP/1.1 200 OK\r\nContent-Encoding: gzip\r\nContent-Type: text/plain\r\nContent-Length: "
                    + gzipData.length + "\r\n\r\n";
            clientSocket.getOutputStream().write(httpResponse.getBytes());
            clientSocket.getOutputStream().write(gzipData);
        }
    }

    /**
     * Handles the GET and POST requests for file retrieval and creation.
     * 
     * @param directoryPath The path to the directory where the file is located or
     *                      will be created.
     * @param clientSocket  The socket connected to the client.
     * @param requestString The HTTP request string received from the client.
     * @throws FileNotFoundException If the file specified in the request does not
     *                               exist.
     * @throws IOException           If an I/O error occurs while reading or writing
     *                               the file.
     */
    private void handleFileRequest(String directoryPath, Socket clientSocket, String requestString)
            throws FileNotFoundException, IOException {

        // Construct the newly wanted absolute path for the given filename

        String fileName = getRequestURI(requestString).split("/")[2];
        directoryPath += fileName;

        // Create a directory instance
        Path directory = Path.of(directoryPath);
        char[] fileBuffer = new char[1024];
        FileReader fileReader;
        FileWriter fileWriter;

        // Get the cotents that will be appended to the given file
        String[] requestParts = requestString.split("\r\n", 0);
        String requestBody = requestParts[requestParts.length - 1].trim();

        boolean fileExists = Files.exists(directory);

        if (fileExists || getRequestType(requestString).equals("POST")) {

            // Jump to the appropriate request type
            switch (getRequestType(requestString)) {
                case "GET" -> {
                    // Create a new fileReader instance to read the existing file
                    fileReader = new FileReader(directoryPath);
                    fileReader.read(fileBuffer);
                    String fileContents = new String(fileBuffer).trim();

                    // Respond with content length, cotent type: application/octet-stream, and body
                    // as the files contents
                    clientSocket.getOutputStream()
                            .write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length:" +
                                    fileContents.length() + "\r\n\r\n" + fileContents + "\r\n").getBytes());
                    fileReader.close();

                }
                case "POST" -> {
                    // Create a new file
                    File file = new File(directoryPath);
                    // Create a new filewriter instance to write to the given filename
                    fileWriter = new FileWriter(file);
                    fileWriter.write(requestBody);
                    fileWriter.flush();
                    fileWriter.close();
                    clientSocket.getOutputStream().write(
                            "HTTP/1.1 201 Created\r\n Content-Type: application/octet-stream\r\n\r\n".getBytes());
                }
                default -> throw new HttpServerException(
                        "Request Type" + getRequestType(requestString) + " is currently not supported");
            }

        } else {
            clientSocket.getOutputStream().write(
                    "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n".getBytes());
        }
    }

    /**
     * helper that validates which operation is to be executed by the server
     * 
     * @param path The path present in the request line
     * @return serverOperation An enum constant is provided depending on the given
     *         path
     */

    private serverOperation defineServerOperation(String requestURI) {
        serverOperation operation;
        if (requestURI.contains("/echo"))
            operation = serverOperation.ECHO;
        else if (requestURI.equals("/user-agent"))
            operation = serverOperation.USER_AGENT;
        else if (requestURI.equals("/"))
            operation = serverOperation.EMPTY;
        else if (requestURI.contains("/files/"))
            operation = serverOperation.FILE;
        else
            operation = serverOperation.INVALID_REQUEST;
        return operation;
    }

    private String[] getRequestLine(String requestString) {
        String[] requestSplit = requestString.split("\r\n", 0);
        return requestSplit[0].split(" ", 0);
    }

    private String getRequestType(String requestString) {
        return getRequestLine(requestString)[0];
    }

    private String getRequestURI(String requestString) {
        return getRequestLine(requestString)[1];
    }
}
