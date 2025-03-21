package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//
public class Gateway extends UnicastRemoteObject implements GatewayInterface{
    private BlockingQueue<String> urlQueue;
    private ArrayList<StorageBarrelInterface> activeBarrels;
    //Gateway
    /**
     * Constructs a new Gateway instance.
     * Initiates the RMI for the Queue and the Barrels.
     * @throws RemoteException if an RMI error occurs.
     */
    public Gateway() throws RemoteException {
        try {
            //Create a shared queue for URLs
            urlQueue = new LinkedBlockingQueue<>();
            activeBarrels = new ArrayList<>();

        } catch (Exception e) {System.out.println("Exception: " + e); }
    }
    
    public void addToQueue(String url) throws RemoteException {
        try {
            urlQueue.put(url);
            System.out.println("[Server]: Added to queue: " + url);
        } catch (InterruptedException e) {
            System.out.println("[Server]: Error adding to queue.");
        }
    }

    public String popFromQueue() throws RemoteException {
        try {
            String url = urlQueue.take();
            System.out.println("[Server]: Popped from queue: " + url);
            return url;
        } catch (InterruptedException e) {
            System.out.println("[Server]: Error popping from queue.");
            return null;
        }
    }

    public ArrayList<String[]> sendMessage(String message, int option) throws RemoteException {
        ArrayList<String[]> result = new ArrayList<String[]>();

        System.out.println(activeBarrels.get(0).teste());

        switch (option) {
            case 1://Admin Page
                break;
        
            case 2://Index URL
                try {
                    String url = URLDecoder.decode(message, StandardCharsets.UTF_8);
                    //Check URL
                    if(!url.toLowerCase().startsWith("http")){
                        ArrayList<String[]> auxResult = new ArrayList<>();
                        auxResult.add(new String[]{"URL not valid"});
                        result= auxResult;
                        break;
                    }

                    System.out.println("Adding URL to queue: " + url);

                    try {
                        this.addToQueue(url);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    //Result
                    ArrayList<String[]> auxResult = new ArrayList<>();
                    auxResult.add(new String[]{"URL added"});
                    result= auxResult;
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                }
            case 3://Search URL
                break;
            case 4://Search Keyword
                break;
            case 5://Top 10
                break;
        }

        return result;
    }

    public ArrayList<StorageBarrelInterface> getBarrels(StorageBarrelInterface mine) throws RemoteException{
        ArrayList<StorageBarrelInterface> result = new ArrayList<>();
		for (StorageBarrelInterface barrel : activeBarrels) {
			if (barrel != mine) {
				result.add(barrel);
			}
		}
		return result;
    }

    public void addBarrel(StorageBarrelInterface barrel) throws RemoteException{
        System.out.println(this.getBarrels(barrel).toString());
        activeBarrels.add(barrel);
    }

    public void removeBarrel(StorageBarrelInterface barrel) throws RemoteException{
        activeBarrels.remove(barrel);
    }



    public static void main(String[] args) {
        // comunica com o cliente:
        String RMI_ADDRESS = "";
        int RMI_PORT = 0;

        //Read information regarding the RMI from "properties.txt"
        String path =  "properties.txt";
        System.out.println("Reading properties from: " + path);
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
            String line;

            while((line = br.readLine()) != null) {
                String[] token = line.split(":");

                if (token[0].trim().equals("RMI Address Gateway")) {
                    RMI_ADDRESS = token[1].trim();
                    System.out.println(RMI_ADDRESS);
                }

                if (token[0].trim().equals("RMI Port")) {
                    RMI_PORT = Integer.parseInt(token[1].trim());
                    System.out.println(RMI_PORT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Starting Gateway...");
            Gateway gateway = new Gateway();

            // Start the RMI registry if not already running
            try {
                LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI Registry created at port " + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("RMI Registry already running.");
            }

            try {
                Naming.rebind("rmi://" + RMI_ADDRESS + ":" + RMI_PORT + "/GATEWAY", gateway);
                System.out.println("Gateway bound to RMI at " + RMI_ADDRESS + ":" + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("Failed to bind Gateway: " + e.getMessage());
            }

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
