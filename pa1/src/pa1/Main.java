
package pa1;
/**
 *
 * @author Joffrey Pannee
 */
public class Main {
    //Error Codes

    public static void main(String[] args) {
        if(args.length < 2){
            System.err.print("Not enough arguments supplied.");
            System.exit(1);            
        }
        
        String clientOrServer = args[0];
        if (null != clientOrServer) {
            switch (clientOrServer) {
                case "server":
                    Server srv = new Server(args);
                    break;
                case "client":
                    Client clt = new Client(args);
                    break;
                default:
                    System.err.println("The first argument must be 'client' or 'server' (without single quotes).");
                    System.exit(1);
            }
        }
    }
}
