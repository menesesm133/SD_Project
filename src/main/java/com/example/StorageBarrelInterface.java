package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.json.JSONObject;

public interface StorageBarrelInterface extends Remote{
    public long getId() throws RemoteException;
    public ArrayList<JSONObject> getInfo() throws RemoteException;
    public void addInfo(String info, boolean downloader) throws RemoteException;
    public ArrayList<String> searchWords(String query) throws RemoteException;
    public ArrayList<String> searchURL(String url) throws RemoteException;
}