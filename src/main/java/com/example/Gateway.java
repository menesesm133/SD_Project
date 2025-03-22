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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//
public class Gateway extends UnicastRemoteObject implements GatewayInterface{
    private BlockingQueue<String> urlQueue;
    // private ArrayList<StorageBarrelInterface> activeBarrels;
    private Map<Long, StorageBarrelInterface> activeBarrels;
    private static Map<String, Boolean> visited;
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
            // activeBarrels = new ArrayList<>();
            activeBarrels = new HashMap<Long, StorageBarrelInterface>();
            visited = new HashMap<String, Boolean>();

        } catch (Exception e) {System.out.println("Exception: " + e); }
    }
    
    @Override
    public void addToQueue(String url) throws RemoteException {
        try {
            if (!visited.containsKey(url)) {
                urlQueue.put(url);
                visited.put(url, true);
                System.out.println("[Server]: Added to queue: " + url);
            } else {
                System.out.println("Url: \"" + url + "\" already added!");
            }
        } catch (InterruptedException e) {
            System.out.println("[Server]: Error adding to queue.");
        }
    }

    @Override
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

    @Override
    public ArrayList<String> sendMessage(String message, int option) throws RemoteException {
        ArrayList<String> result = new ArrayList<String>();

        switch (option) {
            case 1://Admin Page
                break;
        
            case 2://Index URL
                try {
                    String url = URLDecoder.decode(message, StandardCharsets.UTF_8);
                    //Check URL
                    if(!url.toLowerCase().startsWith("http")){
                        ArrayList<String> auxResult = new ArrayList<>();
                        auxResult.add("URL not valid");
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
                    ArrayList<String> auxResult = new ArrayList<>();
                    auxResult.add("URL added");
                    result= auxResult;
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                }
            case 3://Search URL
                try {
                    String url = URLDecoder.decode(message, StandardCharsets.UTF_8);
                    //Check URL
                    if(!url.toLowerCase().startsWith("http")){
                        ArrayList<String> auxResult = new ArrayList<>();
                        auxResult.add("URL not valid");
                        result= auxResult;
                        break;
                    }

                    System.out.println("Searching URL: " + url);

                    try {
                        StorageBarrelInterface b = this.getRandomBarrel(0);
                        result = b.searchURL(url);
                    } catch (RemoteException e) {
                        System.out.println("Error sending info to Barrels");
                    }
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                    e.printStackTrace();
                }
            case 4://Search Keyword
            try {
                System.out.println("Searching: " + message);

                try {
                    StorageBarrelInterface b = this.getRandomBarrel(0);
                    result = b.searchWords(message);
                } catch (RemoteException e) {
                    System.out.println("Error sending info to Barrels");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("Exception indexing: " + e);
            }
        }

        return result;
    }

    @Override
    public ArrayList<StorageBarrelInterface> getBarrels(long myId) throws RemoteException {
        ArrayList<StorageBarrelInterface> result = new ArrayList<>();
        ArrayList<Long> barrelsToRemove = new ArrayList<>();
        
        for (long barrelId : activeBarrels.keySet()) {
            try {
                if (barrelId != myId) {
                    StorageBarrelInterface barrel = activeBarrels.get(barrelId);
                    // Call barrel function to check if still alive
                    barrel.getId();
                    result.add(barrel);
                }
            } catch (Exception e) {
                System.out.println("[Gateway]: Barrel-" + barrelId + " is no longer active!!");
                barrelsToRemove.add(barrelId);
            }
        }
        
        // Now, remove inactive barrels outside the iteration
        for (Long barrelId : barrelsToRemove) {
            removeBarrel(barrelId);
        }
        
        return result;
    }

    
    public StorageBarrelInterface getRandomBarrel(long myId) throws RemoteException {
        ArrayList<StorageBarrelInterface> availableBarrels = getBarrels(myId);

        if (availableBarrels.isEmpty()) {
            System.out.println("No active barrels available.");
            return null;
        }

        Random rand = new Random();
        int randomIndex = rand.nextInt(availableBarrels.size());

        return availableBarrels.get(randomIndex);
    }

    @Override
    public void addBarrel(StorageBarrelInterface barrel, long id) throws RemoteException{
        activeBarrels.put(id, barrel);
    }

    @Override
    public void removeBarrel(long barrelId) throws RemoteException{
        activeBarrels.remove(barrelId);
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