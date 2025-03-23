package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

public class StorageBarrel extends UnicastRemoteObject implements StorageBarrelInterface {
    private static GatewayInterface gateway;
    private static final long id = (long) (Math.random() * 1000 + 1);
    private static StorageBarrel barrel;

    public StorageBarrel() throws RemoteException {
        super();
        // Create folder if it doesn't already exist:
        new File("storage").mkdirs();
        File file = new File("storage/Barrel-" + id + ".json");
        try {
            if (file.createNewFile()) {
                System.out.println("Barrel file created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("❌ Error creating data file for Barrel-" + id);
        }
    }

    @Override
    public long getId() throws RemoteException {
        return id;
    }

    @Override
    public ArrayList<String> searchWords(String query) throws RemoteException {
        ArrayList<String> results = new ArrayList<>();

        try {
            ArrayList<JSONObject> data = getInfo();
            String[] queryTerms = query.toLowerCase().split("\\s+");
            ArrayList<JSONObject> matchingPages = new ArrayList<>();

            // Search through each JSON object
            for (JSONObject page : data) {
                boolean matches = false;

                String title = page.optString("title", "").toLowerCase();
                String text = page.optString("text", "").toLowerCase();
                for (String term : queryTerms) {
                    if (title.contains(term) || text.contains(term)) {
                        matches = true;
                        break;
                    }
                }
                if (matches) {
                    matchingPages.add(page);
                }
            }

            Collections.sort(matchingPages, (obj1, obj2) -> {
                int size1 = obj1.optJSONArray("linksTo") != null ? obj1.getJSONArray("linksTo").length() : 0;
                int size2 = obj2.optJSONArray("linksTo") != null ? obj2.getJSONArray("linksTo").length() : 0;
                return Integer.compare(size2, size1); // Descending order
            });

            for (JSONObject page : matchingPages) {
                results.add(page.getString("url"));
            }

        } catch (Exception e) {
            System.out.println("[Barrel-" + id + "] Error searching for words: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public ArrayList<String> searchURL(String url) throws RemoteException {
        ArrayList<String> results = new ArrayList<>();

        try {
            ArrayList<JSONObject> data = getInfo();

            // Search through each JSON object
            for (JSONObject page : data) {
                if (url.equals(page.getString("url"))) {
                    JSONArray linksTo = page.getJSONArray("linksTo");
                    for (int i = 0; i < linksTo.length(); i++) {
                        results.add(linksTo.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Barrel-" + id + "] Error searching for URL: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public ArrayList<JSONObject> getInfo() throws RemoteException {
        ArrayList<JSONObject> data = new ArrayList<>();
        File file = new File("storage/Barrel-" + id + ".json");

        // Check if the file exists and is not empty
        if (!file.exists() || file.length() == 0) {
            return data; // Return empty list if file is not found or is empty
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Parse JSON content into a JSONArray
            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                data.add(jsonArray.getJSONObject(i));
            }
        } catch (Exception e) {
            System.out.println("[Barrel-" + id + "]: Error reading data file!");
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void addInfo(String info, boolean downloader) throws RemoteException {
        try {
            ArrayList<JSONObject> data = getInfo();
            JSONObject json = new JSONObject(info);

            // Custom check for duplicate JSONObjects by comparing their string
            // representation
            boolean isDuplicate = false;
            for (JSONObject existingJson : data) {
                // Check if duplicate
                if (existingJson.similar(json) || existingJson.toString().equals(json.toString())) {
                    isDuplicate = true;
                }

                String urlAdd = json.getString("url");
                String urlLoop = existingJson.getString("url");
                // Se url adicionado está em [links] de cada um deles: se sim, adicionar url[i]
                // -> linksTo
                JSONArray links = existingJson.getJSONArray("links");
                for (int i = 0; i < links.length(); i++) {
                    if (links.getString(i).equals(urlAdd)) {
                        json.append("linksTo", urlLoop);
                        break;
                    }
                }
                // Se url adicionado possui url[i] em links, url[i][LinksTo] -> url
                links = json.getJSONArray("links");
                for (int i = 0; i < links.length(); i++) {
                    if (links.getString(i).equals(urlLoop)) {
                        existingJson.append("linksTo", urlAdd);
                        break;
                    }
                }
            }

            if (!isDuplicate) {
                data.add(json);

                try (FileWriter fileWriter = new FileWriter("storage/Barrel-" + id + ".json")) {
                    // Write the data to file as a JSON array
                    JSONArray jsonArray = new JSONArray(data);
                    fileWriter.write(jsonArray.toString(4)); // Pretty print with indent level 4
                }
            }

            // Notify other barrels if necessary
            if (downloader) {
                for (StorageBarrelInterface b : gateway.getBarrels(id)) {
                    b.addInfo(info, false);
                }
            }

        } catch (Exception e) {
            System.out.println("[Barrel-" + id + "]: Error adding info!");
        }
    }

    public static void main(String[] args) {
        String path = "properties.txt";
        String RMI_ADDRESS = "";
        int RMI_PORT = 0;

        // Read configuration from properties.txt
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(":");
                if (token[0].trim().equals("RMI Address Gateway")) {
                    RMI_ADDRESS = token[1].trim();
                }
                if (token[0].trim().equals("RMI Port")) {
                    RMI_PORT = Integer.parseInt(token[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            barrel = new StorageBarrel();
            Registry reg = null;

            try {
                reg = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI Registry created at port " + RMI_PORT);
            } catch (RemoteException e) {
                reg = LocateRegistry.getRegistry(RMI_PORT);
                System.out.println("RMI Registry already running.");
            }

            try {
                reg.rebind("BARREL-" + id, barrel);
                System.out.println("Barrel bound to RMI at " + RMI_ADDRESS + ":" + RMI_PORT);
            } catch (RemoteException e) {
                System.out.println("Failed to bind Barrel: " + e.getMessage());
            }

            // Lookup the gateway interface
            gateway = (GatewayInterface) reg.lookup("rmi://" + RMI_ADDRESS + ":" + RMI_PORT + "/GATEWAY");
            gateway.addBarrel(barrel, id);
            System.out.println("[Barrel-" + id + "]: Added to gateway.");

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
}
