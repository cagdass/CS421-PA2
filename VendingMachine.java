//Main and only class
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class VendingMachine {
    // Create a hash map
    static HashMap itemNames;
    static HashMap itemStock;

    public static void main(String[] args) throws Exception {

        int argCount = args.length;

        // no arguments provided -- prompt to rerun again
        if(argCount < 1){
            System.out.println("Not enough arguments.\nUsage: java Testing [<IP_address>] <port_number>");
        }

        // if only the PORT NUMBER is provided then we're the server
        else if(argCount == 1){

            //get the port number
            int portNum = Integer.parseInt(args[0]);
            ServerSocket welcomeSocket = new ServerSocket(portNum);

            //read item_list.txt file
            readItemList();
            //Start getting client requests
            while(true) {

                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());


                String messageFromClient = inFromClient.readLine();
                //capitalizedSentence = clientSentence.toUpperCase() + '\n';
                //outToClient.writeBytes(capitalizedSentence);

                if (messageFromClient.equals("GET ITEM LIST")){
                    //The client wants the item list; give it to the client
                    StringBuffer messageToClient = new StringBuffer();
                    Iterator i = itemNames.entrySet().iterator();

                    while (i.hasNext()){
                        messageFromClient.
                    }

                }  else if (messageFromClient.equals("GET ITEM")){
                    //The client want to get an item. Give it to him
                    Scanner scan = new Scanner(inFromClient.readLine());
                    int itemID = scan.nextInt();
                    int itemCount = scan.nextInt();

                } else {
                    //The client sent a wrong message. Tell him how to send proper messages
                }
            }
        }
        else if(argCount == 2){

            String sentence;
            String modifiedSentence;
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
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
            System.out.println("Too many arguments.\nUsage: java VendingMachine [<IP_address>] <port_number>");
        }




    }

    private static void readItemList() {
        File itemListFile = new File("item_list.txt");

        try {
            Scanner scan = new Scanner(itemListFile);
            itemNames = new HashMap();
            itemStock = new HashMap();
            while(scan.hasNext()){

                int id = scan.nextInt();
                String name = scan.next();
                int stock = scan.nextInt();
                itemNames.put(new Integer(id), name);
                itemStock.put(new Integer(id), new Integer(stock));

            }
            scan.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}

