package com.example.demo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GatewayInterface extends Remote {
    void addToQueue(String url) throws RemoteException;

    String popFromQueue() throws RemoteException;

    ArrayList<String> sendMessage(String message, int option) throws RemoteException;

    ArrayList<StorageBarrelInterface> getBarrels(long myId) throws RemoteException;

    StorageBarrelInterface getRandomBarrel(long myId) throws RemoteException;

    void addBarrel(StorageBarrelInterface barrel, long id) throws RemoteException;

    void removeBarrel(long barrelId) throws RemoteException;

    // New methods for callback
    void registerAdminCallback(AdminCallback callback) throws RemoteException;

    void unregisterAdminCallback(AdminCallback callback) throws RemoteException;
}