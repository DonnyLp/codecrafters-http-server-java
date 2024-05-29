import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 4221;
    private static final int THREAD_SIZE = 3;
  

    public static void main(String[] args) {
        
        //Create a pool of threads of a fixed size THREAD_SIZE that will serve mutliple concurrent client requests
        ExecutorService threads = Executors.newFixedThreadPool(THREAD_SIZE);

        System.out.println("Server spinning up!");
            
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server started on PORT: " + PORT);
            //enable the SO_REUSEADDR flag
            serverSocket.setReuseAddress(true);
            
            //Execute a new clienthandler instance
            while(true){
                ClientHandler exectutionTask = new ClientHandler(serverSocket.accept());
                threads.execute(exectutionTask);
            } 
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            threads.shutdown();
        }
    }
}