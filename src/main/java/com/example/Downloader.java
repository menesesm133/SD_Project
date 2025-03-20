package com.example;

import org.json.JSONObject;
import org.json.JSONArray;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for creating the downloader threads,
 * extract information from the web pages and send it to the barrels
 * through RMI and get urls from the queue through RMI.
 */
public class Downloader extends Thread{
    private static Map<String, Boolean> visited = new HashMap<String, Boolean>();

    /**
     * Constructs a Downloader thread.
     */
    public Downloader() {
        super("Server: " + (long) (Math.random() * 1000));
    }

    public boolean ValidURL(String url) {
        try {
            URL check = new URL(url);
            check.toURI();
            if (check.getProtocol().equals("http") || check.getProtocol().equals("https")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void run() {
        System.out.println("Downloader thread started: " + this.getName());

        while(true) {
            //Read information regarding the RMI from "properties.txt"
            String path = System.getProperty("user.dir") + File.separator + "SD_Project" + File.separator + "properties.txt";
            String RMI_INFO = "";

            try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
                String line;

                while((line = br.readLine()) != null) {
                    String[] token = line.split(":");

                    if (token[0].trim().equals("RMI Address Gateway")) {
                        RMI_INFO = token[1].trim();
                    }

                    if (token[0].trim().equals("RMI Port Gateway")) {
                        RMI_INFO += ":" + Integer.parseInt(token[1].trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                RMIClientInterface gateway = null;

                try {
                    gateway = (RMIClientInterface) java.rmi.Naming.lookup("rmi://" + RMI_INFO + "/GATEWAY");
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                }

                String url = gateway.popFromQueue();

                if (!ValidURL(url)) {
                    continue;
                }

                if (visited.containsKey(url)) {
                    continue;
                }

                visited.put(url, true);

                JSONObject info = searchURL(url);

                JSONArray links = info.getJSONArray("links");
                for (int i = 0; i < links.length(); i++) {
                    String link = links.getString(i);
                    if (!visited.containsKey(link)) {
                        gateway.sendMessage(link, 2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This function serches a web page using the Jsoup library
     * and extracts the information.
     * @param url The url to be searched.
     * @return The JSONObject with the information of the url.
     */
    private JSONObject searchURL(String url) {
        System.out.println("Searching for: " + url);
        JSONObject json = new JSONObject();

        //Get the url information(title, description, associated urls)
        try {
            //Url
            json.put("url", url);

            //Title
            Document doc = Jsoup.connect(url).get();
            json.put("title", doc.title());

            //Description
            json.put("description", doc.select("meta[property=og:description]").attr("content"));

            //Associated Urls
            Elements links = doc.select("a[href]");
            JSONArray linksArray = new JSONArray();

            for (Element link : links) {
                String absoluteUrl = link.attr("abs:href");
                linksArray.put(absoluteUrl);
            }

            json.put("links", linksArray);

        } catch (IOException e) {
            e.printStackTrace();
            json.put("url", "Not available");
        }
        
        return json;
    }
    
    public static void main(String[] args) {
        try {
            Downloader downloader = new Downloader();
            System.out.println("Starting downloader " + downloader.getName());
            downloader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}