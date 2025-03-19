package com.example;

import com.example.GatewayInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
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
        String path = System.getProperty("user.dir") + File.separator + "SD_Project" + File.separator + "properties.txt";
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

                if (token[0].trim().equals("RMI Port Gateway")) {
                    RMI_INFO += ":" + Integer.parseInt(token[1].trim());
                    System.out.println(RMI_INFO);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RMIClientInterface gateway = null;

        try {
            gateway = (RMIClientInterface) java.rmi.Naming.lookup("rmi://" + RMI_INFO + "/GATEWAY");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        System.out.println(
        "1. Search\n" + //Asks for URL and goes to a menu with the search results
        "2. Index URL to Queue\n" + //Adds a URL to the queue
        "3. Administrator Page\n" + //The admin page gets the status of the system
        "4. Exit\n"); //Ends the client

        Scanner sc = new Scanner(System.in);
        String option = sc.nextLine();

        if(option.equals("4")){
            System.out.println("Exiting...");
        }

        else if(option.equals("3")){
            
        }
        
        else if(option.equals("2")){
            System.out.println("Adding URL: ");
            String url = sc.nextLine();
            try{
                ArrayList<String[]> result = gateway.sendMessage(url, Integer.parseInt(option));
            } catch (Exception e) {
                System.out.println("Exception indexing: " + e);
            }
            System.out.println("URL indexed");

        }
        
        else if(option.equals("1")){
            
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
