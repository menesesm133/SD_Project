package com.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIClient extends UnicastRemoteObject implements RMIClientInterface {
    private static Gateway gate;
    private ArrayList<String[]> result;

    public RMIClient() throws Exception {
        super();
    }
    
    public void AddGateway(Gateway inputGateway) throws RemoteException {
        gate = inputGateway;
    }

    @Override
    public String popFromQueue() throws RemoteException {
        return gate.popFromQueue();
    }

    public ArrayList<String[]> sendMessage(String message, int option) throws RemoteException {
        switch (option) {
            case 1:
                break;
        
            case 2:
                //Check URL
                if(!message.toLowerCase().startsWith("http")){
                    ArrayList<String[]> auxResult = new ArrayList<>();
                    auxResult.add(new String[]{"URL not valid"});
                    result= auxResult;
                    break;
                }

                System.out.println("Adding URL to queue: " + message);
                try {
                    gate.addToQueue(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                //Result
                ArrayList<String[]> auxResult = new ArrayList<>();
                auxResult.add(new String[]{"URL added"});
                result= auxResult;
            }

        return result;
    }
}
