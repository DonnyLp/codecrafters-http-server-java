import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 4221;
    private static final int THREAD_SIZE = 1;
  
    public static void main(String[] args) {
        
        //Create a pool of threads of a fixed sized that will serve concurrent client requests
        ExecutorService threads = Executors.newFixedThreadPool(THREAD_SIZE);
       
        System.out.println("Server spinning up!");

        // //Print out command line arguments
        // for (String item : args){
        //    System.out.println(item);
        // }

        //Try to spin up the server and execute the concurrent tasks
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server started on PORT: " + PORT);
            
            //enable the SO_REUSEADDR flag to allow the resuse of the server address
            serverSocket.setReuseAddress(true);
            
            //Set the directory from the command line arguments
            String directoryPath = "";

            if(args.length > 0){
                directoryPath = args[1];
            }
    
            while(true){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientTask = new ClientHandler(clientSocket, directoryPath);   
                 threads.execute(clientTask);
            } 
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            threads.shutdown();
        }
    }
}