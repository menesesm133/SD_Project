package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the client functionality.
 */
public class Client {
    public Client() throws RemoteException {
        super();
    }

    private void menu(){
        //Read information regarding the RMI from "properties.txt"
        String path = "properties.txt";
        System.out.println("Reading properties from: " + path);
        String RMI_INFO = "";

        try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
            String line;

            while((line = br.readLine()) != null) {
                String[] token = line.split(":");

                if (token[0].trim().equals("RMI Address Gateway")) {
                    RMI_INFO = token[1].trim();
                }

                if (token[0].trim().equals("RMI Port")) {
                    RMI_INFO += ":" + Integer.parseInt(token[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GatewayInterface gateway = null;

        System.out.println("Gateway 1: "+ gateway );

        try {
            gateway = (GatewayInterface) java.rmi.Naming.lookup("rmi://" + RMI_INFO + "/GATEWAY");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while(true){
                System.out.println(
                "1. Search\n" + //Asks for URL and goes to a menu with the search results
                "2. Index URL\n" + //Asks for URL and indexes it
                "3. Administrator Page\n" + //The admin page gets the status of the system
                "4. Exit\n"); //Ends the client

                System.out.println("Choose an option: ");

                String option = reader.readLine();

                if(option.equals("4")){
                    System.out.println("Exiting...");
                    break;
                }

                else if(option.equals("3")){
                    ArrayList<String> result = gateway.sendMessage("Admin", 1);
                    System.out.println("System status: ");
                    for (String s : result) {
                        System.out.println(s);
                    }
                }

                else if(option.equals("2")) {
                    System.out.println("URL to index: ");
                    String url = reader.readLine();
                    try{
                        ArrayList<String> result = gateway.sendMessage(url, 2);
                    } catch (Exception e) {
                        System.out.println("Exception indexing: " + e);
                    }
                }
                
                else if(option.equals("1")){
                    System.out.println("\n1. Search\n" +
                    "2. Exit\n");

                    System.out.println("Choose an option: ");

                    String search = reader.readLine();

                    if (search.equals("2")) {
                        System.out.println("Exiting...");
                        break;
                    }

                    else if (search.equals("1")) {
                        System.out.println("\nWhat do you wish to search for?");
                        String keyword = reader.readLine();

                        if(keyword.toLowerCase().startsWith("http")){
                            try{
                                ArrayList<String> result = gateway.sendMessage(keyword, 3);
                                int count = 0;
                                String read = "";
                            
                                while(!read.equals("3")){
                                    System.out.println("\nSearch results(Page "+ (count+10)/10 +"): \n");
                                    int endIndex = Math.min(result.size(), count + 10);
                                    List<String> subList = result.subList(count, endIndex);
                                    for (String s : subList){
                                        System.out.println(s);
                                    }
                                    System.out.println();
                                    if(result.size() > count+10){
                                        System.out.println("1. Next page ->");
                                    }
                                    if(count >= 10) {
                                        System.out.println("2. <- Previous page");
                                    }
                                    System.out.println("3. Exit search");
                                    read = reader.readLine();
                                    if (read.equals("1") && result.size() > count+10) {
                                        count += 10;
                                    } else if (read.equals("2") && count >= 10) {
                                        count -= 10;
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Exception indexing: " + e);
                            }
                        } else {
                            ArrayList<String> result = gateway.sendMessage(keyword, 4);
                            int count = 0;
                            String read = "";
                            
                            while(!read.equals("3")){
                                System.out.println("\nSearch results(Page "+ (count+10)/10 +"): \n");
                                int endIndex = Math.min(result.size(), count + 10);
                                List<String> subList = result.subList(count, endIndex);
                                for (String s : subList){
                                    System.out.println(s);
                                }
                                System.out.println();
                                if(result.size() > count+10){
                                    System.out.println("1. Next page ->");
                                }
                                if(count >= 10) {
                                    System.out.println("2. <- Previous page");
                                }
                                System.out.println("3. Exit search");
                                read = reader.readLine();
                                if (read.equals("1") && result.size() > count+10) {
                                    count += 10;
                                } else if (read.equals("2") && count >= 10) {
                                    count -= 10;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method of the client.
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("Client started");
            Client c = new Client();
            c.menu();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}