package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//
public class Gateway extends UnicastRemoteObject implements GatewayInterface{
    BlockingQueue<String> urlQueue;
    //Gateway
    /**
     * Constructs a new Gateway instance.
     * Initiates the RMI for the Queue and the Barrels.
     * @throws RemoteException if an RMI error occurs.
     */
    public Gateway() throws RemoteException {
        try {

            // try catch -> para a comunicação com o cliente
            //
            //Create a shared queue for URLs
            urlQueue = new LinkedBlockingQueue<>();

            //RMI Barrel
            // b = new RMIBarrel();
            // Naming.rebind("BARREL", b);
            // b.addClient(g);

        } catch (Exception e) {System.out.println("Exception: " + e); }
    }
    
    @Override
    public void addToQueue(String url) throws RemoteException {
        try {
            urlQueue.put(url);
            System.out.println("[Server]: Added to queue: " + url);
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

    public static void main(String[] args) {
        // comunica com o cliente:
        String RMI_ADDRESS = "";
        int RMI_PORT = 0;

        //Read information regarding the RMI from "properties.txt"
        String path = System.getProperty("user.dir") + File.separator + "SD_Project" + File.separator + "properties.txt";
        System.out.println("Reading properties from: " + path);
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
            String line;

            while((line = br.readLine()) != null) {
                String[] token = line.split(":");

                if (token[0].trim().equals("RMI Address Gateway")) {
                    RMI_ADDRESS = token[1].trim();
                    System.out.println(RMI_ADDRESS);
                }

                if (token[0].trim().equals("RMI Port Gateway")) {
                    RMI_PORT = Integer.parseInt(token[1].trim());
                    System.out.println(RMI_PORT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Starting Gateway...");
            RMIClient client = new RMIClient();
            Gateway gateway = new Gateway();
            client.AddGateway(gateway);

            // Start the RMI registry if not already running
            try {
                LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI Registry created at port " + RMI_PORT);
                Naming.rebind("rmi://" + RMI_ADDRESS + ":" + RMI_PORT + "/GATEWAY", client);

            } catch (RemoteException e) {
                System.out.println("RMI Registry already running.");
            }

            System.out.println("Gateway bound to RMI at " + RMI_ADDRESS + ":" + RMI_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
