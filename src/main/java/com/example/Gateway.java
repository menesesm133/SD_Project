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
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is responsible for the Gateway that will communicate with the
 * barrels and the clients.
 */
public class Gateway extends UnicastRemoteObject implements GatewayInterface {
    private BlockingQueue<String> urlQueue;
    private Map<Long, StorageBarrelInterface> activeBarrels;
    private static Map<String, Boolean> visited;
    private Map<Long, Float> responseTime;
    private Map<String, Integer> topTen;

    // List to store admin callbacks
    private CopyOnWriteArrayList<AdminCallback> adminCallbacks;

    /**
     * Constructs a Gateway.
     * 
     * @throws RemoteException
     */
    public Gateway() throws RemoteException {
        try {
            urlQueue = new LinkedBlockingQueue<>();
            activeBarrels = new HashMap<Long, StorageBarrelInterface>();
            visited = new HashMap<String, Boolean>();
            responseTime = new HashMap<Long, Float>();
            topTen = new HashMap<String, Integer>();
            adminCallbacks = new CopyOnWriteArrayList<>();

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * This function is responsible for registering the admin callback.
     * 
     * @param callback The callback to be registered.
     * @throws RemoteException
     */
    @Override
    public void registerAdminCallback(AdminCallback callback) throws RemoteException {
        if (!adminCallbacks.contains(callback)) {
            adminCallbacks.add(callback);
            System.out.println("[Server]: Admin callback registered");
            // Send initial data
            notifyAdminCallbacks();
        }
    }

    /**
     * This function is responsible for unregistering the admin callback.
     * 
     * @param callback The callback to be unregistered.
     * @throws RemoteException
     */
    @Override
    public void unregisterAdminCallback(AdminCallback callback) throws RemoteException {
        adminCallbacks.remove(callback);
        System.out.println("[Server]: Admin callback unregistered");
    }

    /**
     * This function is responsible for notifying the admin callbacks.
     */
    private void notifyAdminCallbacks() {
        if (adminCallbacks.isEmpty()) {
            return;
        }

        // Generate admin data
        ArrayList<String> adminData = new ArrayList<>();
        adminData.add("---- Admin Page ----");
        adminData.add("Active Barrels: " + activeBarrels.size());
        adminData.add("URL Queue Size: " + urlQueue.size());
        adminData.add("Response Time: ");
        for (Long barrelId : responseTime.keySet()) {
            adminData.add("Barrel-" + barrelId + ": " + responseTime.get(barrelId) + " ms");
        }
        adminData.add("\nTop 10 searched words: ");
        for (String key : topTen.keySet()) {
            adminData.add(key + " - " + topTen.get(key));
        }

        // Notify all registered clients
        for (AdminCallback callback : adminCallbacks) {
            try {
                callback.updateAdminData(adminData);
            } catch (RemoteException e) {
                System.out.println("[Server]: Failed to notify admin client, removing callback");
                adminCallbacks.remove(callback);
            }
        }
    }

    /**
     * This function is responsible for adding a URL to the queue.
     * 
     * @param url The URL to be added to the queue.
     * @throws RemoteException
     */
    @Override
    public void addToQueue(String url) throws RemoteException {
        try {
            if (!visited.containsKey(url)) {
                urlQueue.put(url);
                visited.put(url, true);
                System.out.println("[Server]: Added to queue: " + url);
                notifyAdminCallbacks(); // Notify after adding to queue
            } else {
                System.out.println("Url: \"" + url + "\" already added!");
            }
        } catch (InterruptedException e) {
            System.out.println("[Server]: Error adding to queue.");
        }
    }

    /**
     * This function is responsible for popping a URL from the queue.
     * 
     * @return The URL popped from the queue.
     * @throws RemoteException
     */
    @Override
    public String popFromQueue() throws RemoteException {
        try {
            String url = urlQueue.take();
            System.out.println("[Server]: Popped from queue: " + url);
            notifyAdminCallbacks(); // Notify after popping from queue
            return url;
        } catch (InterruptedException e) {
            System.out.println("[Server]: Error popping from queue.");
            return null;
        }
    }

    /**
     * This function is responsible for sending a message to the Gateway.
     * 
     * @param message The message to be sent.
     * @param option  The option to be used.
     * @return The result of the message.
     * @throws RemoteException
     */
    @Override
    public ArrayList<String> sendMessage(String message, int option) throws RemoteException {
        ArrayList<String> result = new ArrayList<String>();

        switch (option) {
            case 1:// Admin Page
                result.add("---- Admin Page ----");
                result.add("Active Barrels: " + activeBarrels.size());
                result.add("URL Queue Size: " + urlQueue.size());
                result.add("Response Time: ");
                for (Long barrelId : responseTime.keySet()) {
                    result.add("Barrel-" + barrelId + ": " + responseTime.get(barrelId) + " ms");
                }
                result.add("\nTop 10 searched words: ");
                for (String key : topTen.keySet()) {
                    result.add(key + " - " + topTen.get(key));
                }
                break;

            case 2:// Index URL
                try {
                    String url = URLDecoder.decode(message, StandardCharsets.UTF_8);
                    // Check URL
                    if (!url.toLowerCase().startsWith("http")) {
                        ArrayList<String> auxResult = new ArrayList<>();
                        auxResult.add("URL not valid");
                        result = auxResult;
                        break;
                    }

                    System.out.println("Adding URL to queue: " + url);

                    try {
                        this.addToQueue(url);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // Result
                    ArrayList<String> auxResult = new ArrayList<>();
                    auxResult.add("URL added");
                    result = auxResult;
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                }
                break;
            case 3:// Search URL
                try {
                    String url = URLDecoder.decode(message, StandardCharsets.UTF_8);
                    // Check URL
                    if (!url.toLowerCase().startsWith("http")) {
                        ArrayList<String> auxResult = new ArrayList<>();
                        auxResult.add("URL not valid");
                        result = auxResult;
                        break;
                    }

                    System.out.println("Searching URL: " + url);

                    try {
                        StorageBarrelInterface b = this.getRandomBarrel(0);
                        result = b.searchURL(url);
                        notifyAdminCallbacks(); // Notify after search
                    } catch (RemoteException e) {
                        System.out.println("Error sending info to Barrels");
                    }
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                    e.printStackTrace();
                }
                break;
            case 4:// Search Keyword
                try {
                    System.out.println("Searching: " + message);

                    try {
                        StorageBarrelInterface b = this.getRandomBarrel(0);
                        result = b.searchWords(message);
                        topTen.put(message.toLowerCase(), topTen.getOrDefault(message, 0) + 1);
                        notifyAdminCallbacks(); // Notify after search
                    } catch (RemoteException e) {
                        System.out.println("Error sending info to Barrels");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    System.out.println("Exception indexing: " + e);
                }
                break;
        }

        return result;
    }

    /**
     * This function is responsible for getting the active barrels.
     * 
     * @param myId The ID of the barrel.
     * @return The barrels.
     * @throws RemoteException
     */
    @Override
    public ArrayList<StorageBarrelInterface> getBarrels(long myId) throws RemoteException {
        ArrayList<StorageBarrelInterface> result = new ArrayList<>();
        ArrayList<Long> barrelsToRemove = new ArrayList<>();

        for (long barrelId : activeBarrels.keySet()) {
            try {
                if (barrelId != myId) {
                    StorageBarrelInterface barrel = activeBarrels.get(barrelId);
                    // Call barrel function to check if still alive and check response time
                    float begin = System.nanoTime();
                    barrel.getId();
                    float time = (System.nanoTime() - begin) / 10000; // Em decimas de segundo
                    responseTime.put(barrelId, time);
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
            responseTime.remove(barrelId);
        }

        if (!barrelsToRemove.isEmpty()) {
            notifyAdminCallbacks(); // Notify if barrels were removed
        }

        return result;
    }

    /**
     * This function is responsible for getting a random barrel.
     * 
     * @param myId The ID of the barrel.
     * @return The random barrel.
     * @throws RemoteException
     */
    @Override
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

    /**
     * This function is responsible for adding a barrel.
     * 
     * @param barrel The barrel to be added.
     * @param id     The ID of the barrel.
     * @throws RemoteException
     */
    @Override
    public void addBarrel(StorageBarrelInterface barrel, long id) throws RemoteException {
        activeBarrels.put(id, barrel);
        notifyAdminCallbacks(); // Notify after adding barrel
    }

    /**
     * This function is responsible for removing a barrel.
     * 
     * @param barrelId The ID of the barrel to be removed.
     * @throws RemoteException
     */
    @Override
    public void removeBarrel(long barrelId) throws RemoteException {
        activeBarrels.remove(barrelId);
        notifyAdminCallbacks(); // Notify after removing barrel
    }

    /**
     * This function is responsible for starting the Gateway.
     * 
     * @param args The arguments to be used.
     */
    public static void main(String[] args) {
        // comunica com o cliente:
        String RMI_ADDRESS = "";
        int RMI_PORT = 0;

        // Read information regarding the RMI from "properties.txt"
        String path = "properties.txt";
        System.out.println("Reading properties from: " + path);
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;

            while ((line = br.readLine()) != null) {
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
            Registry reg = null;

            // Start the RMI registry if not already running
            try {
                reg = LocateRegistry.createRegistry(RMI_PORT);
                System.setProperty("java.rmi.server.hostname", RMI_ADDRESS);
                System.out.println("RMI Registry created at port " + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("RMI Registry already running.");
            }

            try {
                reg.rebind("rmi://" + RMI_ADDRESS + ":" + RMI_PORT + "/GATEWAY", gateway);
                System.out.println("Gateway bound to RMI at " + RMI_ADDRESS + ":" + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("Failed to bind Gateway: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}