import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.InputMismatchException;

public class VendingMachine{
  //
  ServerSocket serverSocket = null;
  Socket clientSocket = null;
  Socket connection = null;
  ObjectOutputStream out = null;
  ObjectInputStream in = null;
  String message = null;
  HashMap<Integer, String> itemNames = null;
  HashMap<Integer, Integer> itemStock = null;
  HashMap<Integer, Integer> receivedItems = null;

  VendingMachine(){};

  public static void main(String args[]){
    if (args.length == 2){
      VendingMachine client = new VendingMachine();
      client.runClient(args);
    } else if (args.length == 1){
      VendingMachine server = new VendingMachine();
      server.readItemList();
      while (true)
        server.runServer(args);
    } else {
      System.out.println("Usage: java VendingMachine [IP Address] <port>");
    }
  }

  private void runClient(String args[]){
    receivedItems = new HashMap<Integer, Integer>();
    try{
        clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
        System.out.println("Connected to " + args[0] + " at port " + args[1] );
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("The connection is established.");
        String userInput = "";
        do{
            System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT):");
            Scanner scan = new Scanner(System.in);
            userInput = scan.nextLine();
            if (!(userInput.equals("L") || userInput.equals("G") || userInput.equals("Q"))){
              System.out.println("'"+userInput+"'\nWrong usage! Try again");
              continue;
            }
            try{
                if (userInput.equals("L")){
                  sendMessage("GET ITEM LIST\r\n");
                  message = (String)in.readObject();
                  System.out.println("The received message:\n" + message);
                } else if (userInput.equals("G")){
                  try{
                    System.out.print("Give the item id: ");
                    //what if the scan still has leftover lines cuz a stupid user or a good tester put multiple lines as input somehow!
                    scan = new Scanner(System.in);
                    int itemID = scan.nextInt();
                    System.out.print("Give the number of items: ");
                    int itemRequestCount = scan.nextInt();
                    sendMessage("GET ITEM\r\n" + itemID + " " + itemRequestCount);
                    message = (String)in.readObject();
                    System.out.println("The received message:\n" + message);
                    Scanner readLines = new Scanner(message);
                    if (readLines.nextLine().equals("SUCCESS")){
                      receivedItems.put(itemID, (receivedItems.get(itemID) != null) ? receivedItems.get(itemID) + itemRequestCount : itemRequestCount);
                    }
                  }catch (InputMismatchException e){
                    System.out.println("Error: You did not enter an integer");
                  }

                }
            }
            catch(ClassNotFoundException classNot){
                System.err.println("data received in unknown format");
            }
        }while(!userInput.equals("Q"));
        System.out.println("The summary of received items:\n"+getReceivedItems());
    }
    catch(UnknownHostException unknownHost){
        System.err.println("You are trying to connect to an unknown host!");
    }
    catch(IOException ioException){
        ioException.printStackTrace();
    }
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

  private void runServer(String args[]){
    System.out.println("The current list of items:\n" + getItemList());
    try{
        serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.print("Waiting for client... ");
        connection = serverSocket.accept();
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush();
        in = new ObjectInputStream(connection.getInputStream());
        System.out.println("A client is connected.");
        do{
            try{
                message = (String)in.readObject();
                System.out.println("The received message:\n" + message);
                Scanner readLines = new Scanner(message);
                String request = readLines.nextLine();
                if (request.equals("GET ITEM LIST")){
                  sendMessage(getItemList());
                } else if (request.equals("GET ITEM")){
                  try{
                    int itemID = readLines.nextInt();
                    int itemRequestCount = readLines.nextInt();
                    if (itemRequestCount <= 0){
                      System.out.println("Send the message:\n" + "Error: Cannot request nonpositive quantity\r\n");
                      sendMessage("Error: Cannot request nonpositive quantity\r\n");
                    } else if (itemStock.get(itemID) != null && itemStock.get(itemID) >= itemRequestCount){
                      itemStock.put(itemID, itemStock.get(itemID) - itemRequestCount);
                      System.out.println("Send the message:\n" + "SUCCESS\r\n");
                      sendMessage("SUCCESS\r\n");
                    } else {
                      System.out.println("Send the message:\n" + "OUT OF STOCK\r\n");
                      sendMessage("OUT OF STOCK\r\n");
                    }
                  } catch (InputMismatchException e){
                      System.out.println("Error: Cannot extract item integer values");
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

  void sendMessage(String message){
      try{
          out.writeObject(message);
          out.flush();
      }
      catch(IOException ioException){
          ioException.printStackTrace();
      }
  }

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

  private String getItemList(){
    StringBuilder itemList = new StringBuilder("");
    Iterator i = itemNames.entrySet().iterator();
    while (i.hasNext()){
        Map.Entry item = (Map.Entry)i.next();
        itemList.append(item.getKey() + " "
                + itemNames.get(item.getKey()) + " "
                + itemStock.get(item.getKey()) + "\r\n");

    }
    return itemList.toString();
  }
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
