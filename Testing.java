import java.io.*; 
import java.net.*;

public class Testing {
	public static void main(String[] args) throws Exception {

		int al = args.length;

		// no arguments provided -- prompt to rerun again
		if(al == 0){
			System.out.println("Not enough arguments.\nusage: java VendingMachine [<IP_address>] <port_number>");
		}
		// if only the PORT NUMBER is provided then we're the server
		else if(al == 1){
			String clientSentence; 
			String capitalizedSentence;

			ServerSocket welcomeSocket = new ServerSocket(6789);

			while(true) {

				Socket connectionSocket = welcomeSocket.accept();

				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				clientSentence = inFromClient.readLine();

				capitalizedSentence = clientSentence.toUpperCase() + '\n';

				outToClient.writeBytes(capitalizedSentence);

			}
		}
		else if(al == 2){

			String sentence;
			String modifiedSentence;
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			Socket clientSocket = new Socket("178.62.59.61", 6789);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// get user input
			System.out.println("Enter line: ");
			sentence = inFromUser.readLine();

			outToServer.writeBytes(sentence + '\n');

			modifiedSentence = inFromServer.readLine();

			System.out.println("FROM SERVER: " + modifiedSentence); 
			clientSocket.close();
		}
		else{
			System.out.println("Too many arguments.\nusage: java VendingMachine [<IP_address>] <port_number>");
		}
		
		
	

	}
}
