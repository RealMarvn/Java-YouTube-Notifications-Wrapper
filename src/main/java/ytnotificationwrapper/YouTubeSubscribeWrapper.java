package ytnotificationwrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static spark.Spark.*;

public class YouTubeSubscribeWrapper {

    private final Set<String> previousVideos = new HashSet<>();

    private static final String CALLBACK_PATH = "/pubsubcallback";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    ObjectMapper xmlMapper = new XmlMapper();

    public YouTubeSubscribeWrapper() { }

    /**
     * Starts the callback HTTP server and provides feed to consumer
     * upon request
     *
     * @param feedConsumer consumer to provide incoming feeds to
     */
    public void start(Consumer<FinalFeed> feedConsumer, int port) {
        port(port);
        get(CALLBACK_PATH, (req, res) -> {
            String challenge = req.queryParams("hub.challenge");
            if (challenge != null)
                return challenge; // Return back challenge to verify subscription

            return "";
        });

        post(CALLBACK_PATH, (req, res) -> {
            String body = req.body();
            VideoFeed value = xmlMapper.readValue(body, VideoFeed.class);
            if (replaceXml(value) == null) return "";

            feedConsumer.accept(replaceXml(value));
            return "";
        });
    }

    /**
     * Clears the cache of duplicated videos
     */
    public void clearCache(){
        previousVideos.clear();
    }
    /**
     * Is getting the Data from a User by its Name
     *
     * @param googleAuthToken token to be allowed to search for the data
     * @param name string to get the data of a user
     */
    public UserData getUserDataByName(String name, String googleAuthToken) throws IOException {
        String topicUrl = "https://youtube.googleapis.com/youtube/v3/channels?part=snippet%2CcontentDetails%2Cstatistics&forUsername=" + name + "&key=" + googleAuthToken + "&format=json";
        HttpGet request = new HttpGet(topicUrl);
        UserData finalUserData = new UserData();
        CloseableHttpResponse response = httpClient.execute(request);
        JsonObject jsonObject = com.google.gson.JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
        if (jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").toString() == null) {
            return null;
        } else {
            finalUserData.setId(jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").toString().replace("\"", ""));
            finalUserData.setName(jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("snippet").getAsJsonObject().get("title").toString().replace("\"", ""));
            return finalUserData;
        }
    }
    /**
     * Is getting the Data from a User by its ID
     *
     * @param googleAuthToken token to be allowed to search for the data
     * @param id string to get the data of a user
     */
    public UserData getUserDataByID(String id,String googleAuthToken) throws IOException {
        String topicUrl = "https://youtube.googleapis.com/youtube/v3/channels?part=snippet%2CcontentDetails%2Cstatistics&id=" + id + "&key=" + googleAuthToken + "&format=json";
        HttpGet request = new HttpGet(topicUrl);
        CloseableHttpResponse response = httpClient.execute(request);
        UserData finalUserData = new UserData();
        JsonObject jsonObject = com.google.gson.JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
        if (jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").toString() == null) {
            return null;
        } else {
            finalUserData.setName(jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("snippet").getAsJsonObject().get("title").toString().replace("\"", ""));
            finalUserData.setId(jsonObject.get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").toString().replace("\"", ""));
            return finalUserData;
        }
    }

    /**
     * Subscribes to PubSub notifications
     *
     * @param callbackUrl callback url to send feed updates to
     * @param channelId   channel ID to subscribe to (starts with UC)
     */
    public void subscribe(String callbackUrl, String channelId, long leaseSeconds) {
        try {
            Map<String, String> params = Map.of(
                    "hub.callback", callbackUrl + CALLBACK_PATH,
                    "hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channelId,
                    "hub.verify", "async",
                    "hub.mode", "subscribe",
                    "hub.verify_token", "",
                    "hub.secret", "",
                    "hub.lease_seconds", String.valueOf(leaseSeconds));

            doHttpFormRequest("https://pubsubhubbub.appspot.com/subscribe?", "POST", params);
        } catch (IOException ignored) {
        }
    }

    public void renewSubscriptions(String callbackUrl, List<String> channelIds, long leaseSeconds) {
            channelIds.forEach(channelId -> {
                Map<String, String> params = Map.of(
                        "hub.callback", callbackUrl + CALLBACK_PATH,
                        "hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channelId,
                        "hub.verify", "async",
                        "hub.mode", "subscribe",
                        "hub.verify_token", "",
                        "hub.secret", "",
                        "hub.lease_seconds", String.valueOf(leaseSeconds));

                try {
                    doHttpFormRequest("https://pubsubhubbub.appspot.com/subscribe?", "POST", params);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    /**
     * sets the parameter which would be null and parsing them over to FinalFeed
     * to prevent user from using #set.
     *
     * @param value give over the value of the XML to replace the null variables.
     */
    private FinalFeed replaceXml(VideoFeed value) {
        FinalFeed finalFeed = new FinalFeed();
        if (value.getEntry().getDatePublished() == value.getEntry().getDateUpdated()) finalFeed.setNewVideo(true);
        if (previousVideos.contains(value.getEntry().getVideoId())) return null;
        previousVideos.add(value.getEntry().getVideoId());
        finalFeed.setChannelId(value.getEntry().getChannelId());
        finalFeed.setVideoId(value.getEntry().getVideoId());
        finalFeed.setTitle(value.getEntry().getTitle());
        finalFeed.setLink("https://www.youtube.com/watch?v=" + value.getEntry().getVideoId());
        finalFeed.setAuthor(value.getEntry().getAuthor());
        finalFeed.setDatePublished(value.getEntry().getDatePublished());
        finalFeed.setDateUpdated(value.getEntry().getDateUpdated());
        return finalFeed;
    }

    /**
     * Makes a url encoded form HTTP request
     *
     * @param urlStr main URL to send to
     * @param method HTTP method to use
     * @param params query parameters / form parameters
     */
    private void doHttpFormRequest(String urlStr, String method, Map<String, String> params) throws IOException {
        URL url = new URL(urlStr);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod(method); // PUT is another valid option
        http.setDoOutput(true);

        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();

        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
    }

}