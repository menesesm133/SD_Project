package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

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
                    System.out.println(RMI_INFO);
                }

                if (token[0].trim().equals("RMI Port")) {
                    RMI_INFO += ":" + Integer.parseInt(token[1].trim());
                    System.out.println(RMI_INFO);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GatewayInterface gateway = null;

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

                    String search = reader.readLine();

                    if (search.equals("2")) {
                        System.out.println("Exiting...");
                        break;
                    }

                    else if (search.equals("1")) {
                        String keyword = reader.readLine();

                        if(keyword.toLowerCase().startsWith("http")){
                            try{
                                System.out.println("Adding URL to queue: " + keyword);
                                ArrayList<String> result = gateway.sendMessage(keyword, 3);
                                int count = 0;
                                System.out.println("Search results: ");

                                for (String s : result) {
                                    System.out.println(s);
                                    count++;
                                    if(count == 10){
                                        System.out.println("1. Next page ->\n" +
                                        "2. Exit\n");
                                        if (reader.readLine().equals("2")) {
                                            break;
                                        } else if (reader.readLine().equals("1")) {
                                            count = 0;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Exception indexing: " + e);
                            }
                        } else {
                            ArrayList<String> result = gateway.sendMessage(keyword, 4);
                            int count = 0;
                            System.out.println("Search results: ");

                            for (String s : result) {
                                System.out.println(s);
                                count++;
                                if(count == 10){
                                    System.out.println("1. Next page ->\n" +
                                    "2. Exit\n");
                                    if (reader.readLine().equals("2")) {
                                        break;
                                    } else if (reader.readLine().equals("1")) {
                                        count = 0;
                                    }
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