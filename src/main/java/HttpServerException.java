public class HttpServerException extends RuntimeException{
    
    //Create custome HTTP server exception message for unsupported operations
    public HttpServerException(String message){
        super(message);
    }
}