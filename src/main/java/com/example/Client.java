package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for the client side of the project.
 */
public class Client extends UnicastRemoteObject implements AdminCallback {
    @SuppressWarnings("unused")
    private ArrayList<String> currentAdminData = new ArrayList<>();
    private AtomicBoolean adminPageActive = new AtomicBoolean(false);
    @SuppressWarnings("unused")
    private Thread adminUpdateThread;

    public Client() throws RemoteException {
        super();
    }

    /**
     * This method is called by the server to update the admin data.
     * 
     * @param adminData The new admin data.
     */
    @Override
    public void updateAdminData(ArrayList<String> adminData) throws RemoteException {
        this.currentAdminData = adminData;
        if (adminPageActive.get()) {
            clearConsole();
            System.out.println("Admin Page (Auto-Refresh):");
            System.out.println("------------------------------");
            for (String line : adminData) {
                System.out.println(line);
            }
            System.out.println("\nPress Enter to return to main menu...");
        }
    }

    /**
     * This method clears the console.
     */
    private void clearConsole() {
        try {
            String operatingSystem = System.getProperty("os.name");
            if (operatingSystem.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback if clearing doesn't work
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * This method is responsible for the client menu
     * and the interaction with the gateway.
     */
    private void menu() {
        // Read information regarding the RMI from "properties.txt"
        String path = "properties.txt";
        System.out.println("Reading properties from: " + path);
        String RMI_ADDRESS = "";
        int RMI_PORT = 0;

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

        GatewayInterface gateway = null;

        try {
            System.setProperty("java.rmi.server.hostname", RMI_ADDRESS);
            Registry reg = LocateRegistry.getRegistry(RMI_ADDRESS, RMI_PORT);
            System.out.println("RMI Registry attached at port " + RMI_PORT);
            gateway = (GatewayInterface) reg.lookup("rmi://" + RMI_ADDRESS + ":" + RMI_PORT + "/GATEWAY");

            // Register for admin updates
            gateway.registerAdminCallback(this);
            System.out.println("Registered for admin updates");
        } catch (Exception e) {
            System.out.println("Error connecting to gateway!");
            e.printStackTrace();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (true) {
                System.out.println("\n\n\n\n" +
                        "Menu:\n" +
                        "1. Search\n" +
                        "2. Index URL\n" +
                        "3. Administrator Page\n" +
                        "4. Exit\n");

                System.out.println("Choose an option: ");

                String option = reader.readLine();

                if (option.equals("4")) {
                    try {
                        gateway.unregisterAdminCallback(this);
                        System.out.println("Unregistered admin callback");
                    } catch (Exception e) {
                        System.out.println("Error unregistering callback: " + e.getMessage());
                    }
                    System.out.println("Exiting...");
                    break;
                }

                else if (option.equals("3")) {
                    try {
                        // Set flag to show we're viewing admin page
                        adminPageActive.set(true);

                        // Get initial data
                        ArrayList<String> result = gateway.sendMessage("Admin", 1);
                        updateAdminData(result);

                        // Wait for user to press Enter to exit admin page
                        reader.readLine();
                        adminPageActive.set(false);
                    } catch (Exception e) {
                        System.out.println("Error in admin page: " + e.getMessage());
                        adminPageActive.set(false);
                    }
                }

                else if (option.equals("2")) {
                    System.out.println("URL to index: ");
                    String url = reader.readLine();
                    try {
                        ArrayList<String> result = gateway.sendMessage(url, 2);
                        System.out.println(result.get(0));
                    } catch (Exception e) {
                        System.out.println("Exception indexing: " + e);
                    }
                }

                else if (option.equals("1")) {
                    System.out.println("\n1. Search\n" +
                            "2. Exit\n");

                    System.out.println("Choose an option: ");

                    String search = reader.readLine();

                    if (search.equals("2")) {
                        System.out.println("Exiting...");
                        break;
                    }

                    else if (search.equals("1")) {
                        System.out.println("\nWhat do you wish to search for?");
                        String keyword = reader.readLine();

                        if (keyword.toLowerCase().startsWith("http")) {
                            try {
                                ArrayList<String> result = gateway.sendMessage(keyword, 3);
                                int count = 0;
                                String read = "";

                                while (!read.equals("3")) {
                                    System.out.println("\nSearch results(Page " + (count + 10) / 10 + "): \n");
                                    int endIndex = Math.min(result.size(), count + 10);
                                    List<String> subList = result.subList(count, endIndex);
                                    for (String s : subList) {
                                        System.out.println(s);
                                    }
                                    System.out.println();
                                    if (result.size() > count + 10) {
                                        System.out.println("1. Next page ->");
                                    }
                                    if (count >= 10) {
                                        System.out.println("2. <- Previous page");
                                    }
                                    System.out.println("3. Exit search");
                                    read = reader.readLine();
                                    if (read.equals("1") && result.size() > count + 10) {
                                        count += 10;
                                    } else if (read.equals("2") && count >= 10) {
                                        count -= 10;
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Exception indexing: " + e);
                            }
                        } else {
                            ArrayList<String> result = gateway.sendMessage(keyword.toLowerCase(), 4);
                            int count = 0;
                            String read = "";

                            while (!read.equals("3")) {
                                System.out.println("\nSearch results(Page " + (count + 10) / 10 + "): \n");
                                int endIndex = Math.min(result.size(), count + 10);
                                List<String> subList = result.subList(count, endIndex);
                                for (String s : subList) {
                                    System.out.println(s);
                                }
                                System.out.println();
                                if (result.size() > count + 10) {
                                    System.out.println("1. Next page ->");
                                }
                                if (count >= 10) {
                                    System.out.println("2. <- Previous page");
                                }
                                System.out.println("3. Exit search");
                                read = reader.readLine();
                                if (read.equals("1") && result.size() > count + 10) {
                                    count += 10;
                                } else if (read.equals("2") && count >= 10) {
                                    count -= 10;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method of the client.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("Client started");
            Client c = new Client();
            c.menu();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}