package SD_Project.src;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Downloader extends Thread{

    public Downloader() {
        super("Server: " + (long) (Math.random() * 1000));
    }
    
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
            Elements paragraphs = doc.select("p");
            StringBuilder textBuilder = new StringBuilder();

            for (Element paragraph : paragraphs) {
                textBuilder.append(paragraph.text()).append("\n");
            }

            String text = textBuilder.toString();
            json.put("text", text);

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
    
}