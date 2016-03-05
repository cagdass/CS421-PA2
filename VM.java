import java.io.*;
import java.net.*;
import java.util.*;

public class VendingMachine{

    static HashMap<Integer, String> itemNames;
    static HashMap<Integer, Integer> itemStock;

    // hashmap to keep track of the received items
 	static HashMap<Integer, Integer> receivedItems;
	
	public static void main(String[] args){

		int argCount = args.length;
		String msg = "";

		ServerSocket providerSocket = null;
	    Socket connection = null;
	    Socket requestSocket = null;
	    ObjectOutputStream out = null;
	    ObjectInputStream in = null;
	    String message = "";
	    Scanner scan = new Scanner(System.in);

        // no arguments provided -- prompt to rerun again
        if(argCount < 1){
            System.out.println("Not enough arguments.\nUsage: java Testing [<IP_address>] <port_number>");
        }

        else if(argCount == 1){
        	
        	//read item_list.txt file
            readItemList();
            System.out.println("item list.txt is read");

            System.out.println("The current list of items:");
            Iterator i = itemNames.entrySet().iterator();
            while(i.hasNext()){
                Map.Entry item = (Map.Entry)i.next();
                System.out.println(item.getKey()+" "+itemNames.get(item.getKey())+" "+itemStock.get(item.getKey()));
            }
            System.out.println();
            System.out.print("Waiting for a client... ");

        	while(true){
        		try{
		            providerSocket = new ServerSocket(Integer.parseInt(args[0]), 10);
		            // System.out.println("Waiting for connection");
		            connection = providerSocket.accept();
		            System.out.println("A client is connected");
		            //3. get Input and Output streams
		            out = new ObjectOutputStream(connection.getOutputStream());
		            out.flush();
		            in = new ObjectInputStream(connection.getInputStream());
		            
		            //4. The two parts communicate via the input and output streams
		            do{
		                try{
		                    message = (String)in.readObject();
		                    System.out.println("The received message: ");
		                    // System.out.println("client>" + message);
		                    // System.out.println(message);
		                    if(message.equals("GET ITEM LIST\r\n\r\n")){
		                    	System.out.println("  GET ITEM LIST");
		                    	System.out.println("Send the message");

		                    	//The client wants the item list; give it to the client
		                    	System.out.println("  ITEM LIST");
		                        StringBuilder messageToClient = new StringBuilder("ITEM LIST\r\n");
		                        i = itemNames.entrySet().iterator();
		                        while (i.hasNext()){
		                            Map.Entry item = (Map.Entry)i.next();

		                            messageToClient.append(item.getKey() + " "
		                                    + itemNames.get(item.getKey()) + " "
		                                    + itemStock.get(item.getKey()) + "\r\n");

		                            System.out.println("  " + item.getKey() + " " + itemNames.get(item.getKey()) + " " + itemStock.get(item.getKey()));

		                        }
		                        messageToClient.append("\r\n");

		                        out.writeObject(messageToClient.toString());
 
		                    }

		                    StringReader reader = new StringReader(message);
						    BufferedReader br = new BufferedReader(reader);
						    String line = br.readLine();
						    if(line != null && line.equals("GET ITEM"))
						    {
						    	line = br.readLine();
						    	String[] nums = line.split(" ");
						    	int id = Integer.parseInt(nums[0]);
						    	int count = Integer.parseInt(nums[1]);

						    	if (itemStock.get(id) != null && itemStock.get(id) >= count){
		                            //update the stock
		                            itemStock.put(id, itemStock.get(id) - count);
		                            //send success message to client
		                            System.out.println("Send the message:");
		                            System.out.println("  SUCCESS");
		                            // outToClient.writeBytes("SUCCESS\r\n");
		                            out.writeObject("SUCCESS\r\n\r\n");
		                        } else {
		                            System.out.println("Send the message:");
		                            System.out.println("  OUT OF STOCK");
		                            out.writeObject("OUT OF STOCK\r\n\r\n");

		                        }
						    }
		                }
		                catch(ClassNotFoundException classnot){
		                    System.err.println("Data received in unknown format");
		                }
		            }while(!message.equals("QUIT"));
		        }
		        catch(Exception e){
		            // e.printStackTrace();
		        }
		        finally{
		            //4: Closing connection
		            try{
		                in.close();
		                out.close();
		                providerSocket.close();
		            }
		            catch(IOException ioException){
		                ioException.printStackTrace();
		            }
		        }
        	}
        	
	    }

	    else if(argCount == 2){
	    	try{
	            //1. creating a socket to connect to the server
	            requestSocket = new Socket(args[0], Integer.parseInt(args[1]));
	            System.out.println("The connection is established.");
	            //2. get Input and Output streams
	            out = new ObjectOutputStream(requestSocket.getOutputStream());
	            out.flush();
	            in = new ObjectInputStream(requestSocket.getInputStream());
	            //3: Communicating with the server
	            do{
	                try{

	                	String id = "";
	                	String count = "";

	                    try{
	                    	System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT): ");
	                    	msg = scan.nextLine();

	                    	if(msg.equals("L")){
	                    		msg = "GET ITEM LIST\r\n\r\n";

	                    		out.writeObject(msg);
				            	out.flush();
	                    	}
	                    	else if(msg.equals("G")){
	                    		System.out.print("Give the item id: ");
	                    		id = scan.nextLine();
	                    		System.out.print("Give the number of items: ");
	                    		count = scan.nextLine();
	                    		msg = "GET ITEM\r\n" + id + " " + count + "\r\n\r\n";

	                    		out.writeObject(msg);
				            	out.flush();
	                    	}

				            
				        }
				        catch(IOException ioException){
				            ioException.printStackTrace();
				        }

				        message = (String)in.readObject();
	                    System.out.println("The received message:");

	                    StringReader reader = new StringReader(message);
					    BufferedReader br = new BufferedReader(reader);
					    String line;
					    while((line=br.readLine())!=null)
					    {
					    	String nl = line.replaceAll("\r\n", "");
					        if(!nl.equals("")){
					        	// if(nl.equals("SUCCESS")){
					        		// System.out.println(id);
					        		// System.out.println(receivedItems.get(Integer.parseInt(id)));
					        		// if(!receivedItems.containsKey(Integer.parseInt(id))){
					        			// receivedItems.put(Integer.parseInt(id), Integer.parseInt(count));
					        		// }
					        		// else{
					        			// receivedItems.put(Integer.parseInt(id) , receivedItems.get(Integer.parseInt(id)) - Integer.parseInt(count));
					        		// }
					        	// }
					        	System.out.println("  " + nl);
					        }
					    }

	                }
	                catch(ClassNotFoundException classNot){
	                    System.err.println("data received in unknown format");
	                }
	            }while(!message.equals("Q"));
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
	                requestSocket.close();
	            }
	            catch(Exception e){
	                e.printStackTrace();
	            }
        	}
	    }

	    System.out.println("The summary of received items:");

    	Iterator i = itemNames.entrySet().iterator();
        while(i.hasNext()){
            Map.Entry item = (Map.Entry)i.next();
            System.out.println("  " + item.getKey() + " " + receivedItems.get(item.getKey()));
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