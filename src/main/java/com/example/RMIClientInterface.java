package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIClientInterface extends Remote {
    public void AddGateway(Gateway inputGateway) throws RemoteException;
    public ArrayList<String[]> sendMessage(String message, int option) throws RemoteException;
}
