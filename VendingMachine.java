import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.InputMismatchException;

public class VendingMachine{
  //Local varialbles that will be used by either the client and/or the server
  //Will be used by client instantiations only
  Socket clientSocket = null;
  HashMap<Integer, Integer> receivedItems = null;
  //Will be used by server instantiations only
  ServerSocket serverSocket = null;
  HashMap<Integer, String> itemNames = null;
  HashMap<Integer, Integer> itemStock = null;
  //Will be used by both client and server instantiations
  Socket connection = null;
  ObjectOutputStream out = null;
  ObjectInputStream in = null;
  String message = null;

  //Empty constuctor
  VendingMachine(){};

  public static void main(String args[]){
    //This will be a client
    if (args.length == 2){
      VendingMachine client = new VendingMachine();
      client.runClient(args);
    }
    //This will be a server
    else if (args.length == 1){
      VendingMachine server = new VendingMachine();
      //Read the inventory from the text file
      server.readItemList();
      //Keep on running
      while (true)
        server.runServer(args);
    }
    //Give an error message and show the correct usage
    else {
      System.out.println("Usage: java VendingMachine [IP Address] <port>");
    }
  }

  //This will be the client main running method
  private void runClient(String args[]){
    //Initiate the HashMap
    receivedItems = new HashMap<Integer, Integer>();
    //This has to be in a try block to catch failed failed connection initiations or broken connections after they have started
    try{
        //First argument is the IP address, and second is the port number
        clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
        //Now setup input and output steams
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("The connection is established.");
        String userInput = "";
        do{
            System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT):");
            Scanner scan = new Scanner(System.in);
            userInput = scan.nextLine();
            //Check that user has entered one of the correct options
            if (!(userInput.equals("L") || userInput.equals("G") || userInput.equals("Q"))){
              System.out.println("'"+userInput+"'\nWrong usage! Try again");
              continue;
            }
            //catches when the received message is not in a String class format
            try{
                if (userInput.equals("L")){
                  //send request to server
                  sendMessage("GET ITEM LIST\r\n\r\n");
                  //wait until server replies
                  message = (String)in.readObject();
                  System.out.println("The received message:\n" + message);
                } else if (userInput.equals("G")){
                  //This catches if the user enters non-integer values
                  try{
                    System.out.print("Give the item id: ");
                    //what if the scan still has leftover lines cuz a stupid user or a good tester put multiple lines as input somehow! So it is better to re-initalize scan
                    scan = new Scanner(System.in);
                    int itemID = scan.nextInt();
                    System.out.print("Give the number of items: ");
                    int itemRequestCount = scan.nextInt();
                    //Send user's request to server
                    sendMessage("GET ITEM\r\n" + itemID + " " + itemRequestCount + "\r\n\r\n");
                    //wait for server's reply
                    message = (String)in.readObject();
                    System.out.println("The received message:\n" + message);
                    //Use a Scanner object to get rid of line delimiters
                    Scanner readLines = new Scanner(message);
                    //if the requested items were vended successfully, then add their quantity to the receivedItems HashMap
                    if (readLines.nextLine().equals("SUCCESS")){
                      receivedItems.put(itemID, (receivedItems.get(itemID) != null) ? receivedItems.get(itemID) + itemRequestCount : itemRequestCount);
                    }
                  } catch (InputMismatchException e){
                    System.out.println("Error: You did not enter an integer");
                  }

                }
            }
            catch(ClassNotFoundException classNot){
                System.err.println("Error: Data received was not in a String class format");
            }
        }while(!userInput.equals("Q"));
        //Print the list of receivedItems before quitting
        System.out.println("The summary of received items:\n"+getReceivedItems());
    }
    catch(UnknownHostException unknownHost){
        System.err.println("You are trying to connect to an unknown host!");
    }
    catch(IOException ioException){
        ioException.printStackTrace();
    }
    //Of course, make sure connections are closed
    finally{
        try{
            in.close();
            out.close();
            clientSocket.close();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
  }

  //This will be run by the server
  private void runServer(String args[]){
    //First print the list of items in the stock currently
    System.out.println("The current list of items:\n" + getItemList());
    //Catch if client terminated the connection
    try{
        serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.print("Waiting for client... ");
        //Setup in and out streams
        connection = serverSocket.accept();
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush();
        in = new ObjectInputStream(connection.getInputStream());
        System.out.println("A client is connected.");
        //Keep on serving this client until an exception due terminating the connection is thrown
        do{
            try{
                //Get the request from the client
                message = (String)in.readObject();
                System.out.println("The received message:\n" + message);
                //Use a Scanner object to get rid of line delimiters
                Scanner readLines = new Scanner(message);
                String request = readLines.nextLine();
                if (request.equals("GET ITEM LIST")){
                  // Print sent message
                  System.out.print("Send the message:\n" + getItemList() + "\r\n");
                  sendMessage(getItemList());
                } else if (request.equals("GET ITEM")){
                  //Check that the ID and quantity requested are integers. If not then catch the exception and send the client an error message
                  try{
                    int itemID = readLines.nextInt();
                    int itemRequestCount = readLines.nextInt();
                    //Don't accept negative or zero quantities
                    if (itemRequestCount <= 0){
                      System.out.print("Send the message:\n" + "Error: Cannot request nonpositive quantity\r\n");
                      sendMessage("Error: Cannot request nonpositive quantity\r\n");
                    }
                    //Check if the item is in the stock and that there is enough availability
                    else if (itemStock.get(itemID) != null && itemStock.get(itemID) >= itemRequestCount){
                      itemStock.put(itemID, itemStock.get(itemID) - itemRequestCount);
                      System.out.print("Send the message:\n" + "SUCCESS\r\n\r\n");
                      sendMessage("SUCCESS\r\n\r\n");
                    } else {
                      System.out.print("Send the message:\n" + "OUT OF STOCK\r\n\r\n");
                      sendMessage("OUT OF STOCK\r\n\r\n");
                    }
                  } catch (InputMismatchException e){
                      System.out.print("Error: Cannot extract item integer values");
                      sendMessage("Bad request content: Cannont parse integer values of items\r\n");
                  }

                } else {
                  //ERROR
                  sendMessage("Bad request header\r\n");
                }

            }
            catch(ClassNotFoundException classnot){
                System.err.println("Data received in unknown format");
            }
        }while(true);
    }
    catch(IOException ioException){
        System.out.println("The client has terminated the connection.");
        //ioException.printStackTrace();
    }
    // And close the connections of course
    finally{
        try{
            in.close();
            out.close();
            serverSocket.close();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
  }
  //A generic message sending method for both client and server
  void sendMessage(String message) throws IOException{
    out.writeObject(message);
    out.flush();
  }
  //This is a helper method for the server
  private void readItemList() {
      File itemListFile = new File("item_list.txt");
      try {
          Scanner scan = new Scanner(itemListFile);
          itemNames = new HashMap<Integer, String>();
          itemStock = new HashMap<Integer, Integer>();
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

  //This is a helper method for the server
  private String getItemList(){
    StringBuilder itemList = new StringBuilder("ITEM LIST\r\n");
    Iterator i = itemNames.entrySet().iterator();
    while (i.hasNext()){
        Map.Entry item = (Map.Entry)i.next();
        itemList.append(item.getKey() + " "
                + itemNames.get(item.getKey()) + " "
                + itemStock.get(item.getKey()) + "\r\n");

    }
    itemList.append("\r\n");
    return itemList.toString();
  }

  //This is a helper method for the client
  private String getReceivedItems(){
    StringBuilder itemList = new StringBuilder("");
    Iterator i = receivedItems.entrySet().iterator();
    while (i.hasNext()){
        Map.Entry item = (Map.Entry)i.next();
        itemList.append(item.getKey() + " "
                + receivedItems.get(item.getKey()) + "\r\n");

    }
    return itemList.toString();
  }
}
