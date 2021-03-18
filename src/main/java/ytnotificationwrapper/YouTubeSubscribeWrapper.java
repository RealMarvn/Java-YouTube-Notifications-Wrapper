package ytnotificationwrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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