//Main and only class
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class VendingMachine {
    // Create a hash map
    static HashMap<Integer, String> itemNames;
    static HashMap<Integer, Integer> itemStock;

    // hashmap to keep track of the received items
    static HashMap<String, String> receivedItems;

    public static void main(String[] args)  {

        int argCount = args.length;

        // no arguments provided -- prompt to rerun again
        if(argCount < 1){
            System.out.println("Not enough arguments.\nUsage: java Testing [<IP_address>] <port_number>");
        }

        // if only the PORT NUMBER is provided then we're the server
        else if(argCount == 1){

            //get the port number
            int portNum = Integer.parseInt(args[0]);

            //read item_list.txt file
            readItemList();
            System.out.println("item list.txt is read");

            //Start getting client requests
            while(true) {
                System.out.println("The current list of items:");
                Iterator i = itemNames.entrySet().iterator();
                while(i.hasNext()){
                    Map.Entry item = (Map.Entry)i.next();
                    System.out.println(item.getKey()+" "+itemNames.get(item.getKey())+" "+itemStock.get(item.getKey()));
                }
                System.out.println();
                System.out.print("Waiting for a client... ");
                try {
                    ServerSocket welcomeSocket = new ServerSocket(portNum);
                    Socket connectionSocket = welcomeSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                    System.out.println("A client has connected.");
                    String messageFromClient = inFromClient.readLine();

                    if(messageFromClient == null){
                        System.out.println("The client has terminated the connection.");
                        continue;
                    }

                    System.out.println("The received message:");
                    System.out.println(messageFromClient.toString());

                    if (messageFromClient.equals("GET ITEM LIST")){

                        //The client wants the item list; give it to the client
                        StringBuilder messageToClient = new StringBuilder("ITEM LIST\r\n");
                        i = itemNames.entrySet().iterator();
                        while (i.hasNext()){
                            Map.Entry item = (Map.Entry)i.next();

                            messageToClient.append(item.getKey() + " "
                                    + itemNames.get(item.getKey()) + " "
                                    + itemStock.get(item.getKey()) + "\r\n");

                        }
                        messageToClient.append("\r\n");
                        System.out.println("Send the message:");
                        System.out.println(messageToClient.toString());
                        outToClient.writeBytes(messageToClient.toString());

                    }  else if (messageFromClient.equals("GET ITEM")){
                        //The client want to get an item. Give it to him
                        Scanner scan = new Scanner(inFromClient.readLine());
                        int itemID = scan.nextInt();
                        int itemCount = scan.nextInt();

                        //if the item with this ID actually exists, and has stock more than requested
                        if (itemStock.get(itemID) != null && itemStock.get(itemID) >= itemCount){
                            //update the stock
                            itemStock.put(itemID, itemStock.get(itemID) - itemCount);
                            //send success message to client
                            System.out.println("Send the message:");
                            System.out.println("SUCCESS\r\n");
                            outToClient.writeBytes("SUCCESS\r\n");
                        } else {
                            System.out.println("Send the message:");
                            System.out.println("OUT OF STOCK\r\n");
                            outToClient.writeBytes("OUT OF STOCK\r\n");

                        }

                    } 
                    else {
                        //The client sent a wrong message. Tell him how to send proper messages
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("The client has terminated the connection.");

                }


            }
        }
        else if(argCount == 2){

            // variables for client's request and server's reply
            String reply = "";
            String request = "";
            String userInput = "";


            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            // initialize client socket to be null initially and initialize it while handling exceptions to avoid program from crashing
            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;

            try{
                clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
            catch(Exception e){
                // print possible reasons and exit the program
                // System.out.println("Given hostname cannot be found, rerun after eliminating the following.\nPossible reasons:\tNo Internet connection.\tHostname leads to no server.");
                // System.exit(1);
                e.printStackTrace();
            }

            

            System.out.println("The connection is established.");

            while (true){


                System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT: ");

                // get the user input - and convert to uppercase to accept lowercase commands as well;
                try{
                    userInput = inFromUser.readLine().toUpperCase();
                }
                catch(IOException e){
                    e.printStackTrace();
                    System.out.println("IOException");
                }

                // the request is to get the item list
                if(userInput.equals("L")){
                    
                    request = "GET ITEM LIST\r\n\r\n";
                    
                    // send the request to the server
                    try{
                        outToServer.writeBytes(request);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.out.println("IOException");
                    }
                    System.out.println("The received message:");

                    while(true){
                        try{
                            // did this whole block for tabs thingy might revert to inFromServer.toString()
                            System.out.print(inFromServer.readLine());
                        }
                        catch(Exception e){
                            break;
                        }
                    }
                }
                // the request is to get a specific item, append the item id and quantity to the request
                else if(userInput.equals("G")){
                    
                    // item id and quantity to be added to the request
                    String id = "";
                    String quantity = "";

                    System.out.print("Give the item id: ");
                    try{
                        id = inFromUser.readLine();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.out.println("IOException");
                    }
                    System.out.print("Give the number of items: ");
                    try{
                        quantity = inFromUser.readLine();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.out.println("IOException");
                    }

                    request = "GET ITEM\r\n" + id + " " + quantity + "\r\n\r\n";
                    
                    // send the request to the server
                    try{
                        outToServer.writeBytes(request);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.out.println("IOException");
                    }

                    System.out.println("The received message:");

                    try{
                        reply = inFromServer.readLine();    
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.out.println("IOException");
                    }
                    
                    System.out.println(reply);
                    if(reply.equals("SUCCESS")){
                        receivedItems.put(id, quantity);
                    }
                }
                else if(userInput.equals("Q")){
                    System.out.println("The summary of received items: ");
                    Iterator i = null;
                    if (receivedItems != null)
                        i = receivedItems.entrySet().iterator();
                    if (i != null){
                        while(i.hasNext()){
                            Map.Entry item = (Map.Entry)i.next();
                            System.out.println(item.getKey() + " " + item.getValue());
                        }    
                    }
                    
                    break;
                }
                else{
                    System.out.println("Your input does not match L, G or Q.");
                    continue;
                }


            }
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

