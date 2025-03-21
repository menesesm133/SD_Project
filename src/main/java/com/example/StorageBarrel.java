package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class StorageBarrel extends UnicastRemoteObject implements StorageBarrelInterface {
    private static GatewayInterface gateway;
    private static final long id = (long) (Math.random() * 1000);
    private static StorageBarrel barrel;

    public StorageBarrel() throws RemoteException {
        super();
    }

    @Override
    public String teste() throws RemoteException {
        return "Teste";
    }

    public static void main(String[] args) {
        // Shutdown hook to remove the barrel when the process stops
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (gateway != null) {
                    gateway.removeBarrel(barrel);
                    System.out.println("[Barrel-" + id + "]: Successfully unregistered.");
                }
            } catch (RemoteException e) {
                System.out.println("[Barrel-" + id + "]: Error unregistering barrel.");
            }
        }));

        String path = "properties.txt";
        String RMI_SERVER = "";
        String RMI_PORT = "";

        // Read configuration from properties.txt
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(":");
                if (token[0].trim().equals("RMI Address Gateway")) {
                    RMI_SERVER = token[1].trim();
                }
                if (token[0].trim().equals("RMI Port")) {
                    RMI_PORT = token[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            barrel = new StorageBarrel();

            try {
                LocateRegistry.createRegistry(Integer.parseInt(RMI_PORT));
                System.out.println("RMI Registry created at port " + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("RMI Registry already running.");
            }

            try {
                Naming.rebind("rmi://" + RMI_SERVER + ":" + RMI_PORT + "/BARREL-" + id, barrel);
                System.out.println("Barrel bound to RMI at " + RMI_SERVER + ":" + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("Failed to bind Barrel: " + e.getMessage());
            }

            // Lookup the gateway interface
            gateway = (GatewayInterface) Naming.lookup("rmi://" + RMI_SERVER + ":" + RMI_PORT + "/GATEWAY");
            gateway.addBarrel(barrel);
            System.out.println("[Barrel-" + id + "]: Added to gateway.");
            
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
}
