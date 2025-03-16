package com.example;

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
            // comunica com o cliente:
            Gateway serverClient = new Gateway();
            LocateRegistry.createRegistry(1099).rebind("serverClient", serverClient);

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
    
    public void addToQueue(String url) throws RemoteException {
		// System.out.println("[Server]: Adding to queue: " + url);
		try {
			urlQueue.put(url);
		} catch (InterruptedException e) {
			System.out.println("[Server]: Error adding to queue.");
		}
	}
}
