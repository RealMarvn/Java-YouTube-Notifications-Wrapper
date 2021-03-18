# Java YouTube Data API Subscribe Notifications wrapper

This is a simple wrapper for the YouTube Data API v3 notifications. As there are many people dont want to deal with this api , I decided to create a wrapper for you :D. Hope you like it.

# 🎉 Basic Usage
The following example shows you how to get notifications, subscribing to new channel and renewing the leasing time.
```Java
        int port = 8080;
        int leaseSeconds = 60 * 60 * 24 * 7;
        List<String> channelIdList = new ArrayList<>();

        YouTubeSubscribeWrapper youTubeSubscribeWrapper = new YouTubeSubscribeWrapper();
        youTubeSubscribeWrapper.subscribe("http://example.com", "UCJhjE7wbdYAae1G25m0tHAA", leaseSeconds);
        youTubeSubscribeWrapper.renewSubscriptions("http://example.com", channelIdList,  864000);
        youTubeSubscribeWrapper.start(feed -> {
            if(feed.isNewVideo()){
                System.out.println("NEW VIDEO!");
            }

            System.out.println(feed.getVideoId());
            System.out.println(feed.getChannelId());
            System.out.println(feed.getTitle());
            System.out.println(feed.getLink());
            System.out.println(feed.getAuthor());
        }, port);
```
Note that maximum lease time is 10 days, this will require you to create your own re-subscription scheduler. There is also a method you can use which is called #renewSubscriptions().


# 📋 Libarys which are used in this Wrapper
- SparkJava
- Lombok
- jackson-dataformat-xml
- woodstox-core

