![dickpic](https://user-images.githubusercontent.com/67916561/111707819-10265280-8845-11eb-90f4-d2e6e0715342.png)

# Java YouTube Data Subscribe Notifications wrapper

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
        if(feed.isNewVideo())
            System.out.println("NEW VIDEO!");
        

        System.out.println(feed.getVideoId());
        System.out.println(feed.getChannelId()
        System.out.println(feed.getTitle());
        System.out.println(feed.getLink());
        System.out.println(feed.getAuthor());
        }, port);
```
Note that maximum lease time is 10 days, this will require you to create your own re-subscription scheduler. There is also a method you can use which is called #renewSubscriptions().

# 📦 Download / Installation
The recommended way to get the wrapper is to use a build manager, like Gradle or Maven.
Gradle
```java
 repositories { 
    mavenCentral()
    maven { url "https://jitpack.io" } 
    }
    
dependencies { implementation 'com.github.Realmarvn:Java-YouTube-Data-Notifications-Wrapper:v1.0.2' }
```

Maven
```java
     <dependency>
         <groupId>com.github.RealMarvn</groupId>
         <artifactId>Java-YouTube-Data-Notifications-Wrapper</artifactId>
         <version>v1.0.2</version>
     </dependency>
```

# 📒 GetUserData
To get the user data from a specific youtube channel you need to use one of these two methods. You also require a Google API token which you can get at the Google Developer hub.
```Java
    YouTubeSubscribeWrapper youTubeSubscribeWrapper = new YouTubeSubscribeWrapper();
    youTubeSubscribeWrapper.getUserDataByName("shroud", "YourGoogleApiToken").getId; // Will give you back the ID of the channel you are searching
    youTubeSubscribeWrapper.getUserDataById("UCtGQtxNnG1b2gV5-b1y2xgQ", "YourGoogleApiToken").getName; // Will give you back the name of the channel you are searching
```
With the API token you can search up these informations about 10.000 times a day. 
NOTE: The ID of a channel is always 24 characters long and starts with UC.


# 🔧 Cache
If you want to keep the cache clean you can just call the clearCache method.
```Java
    YouTubeSubscribeWrapper youTubeSubscribeWrapper = new YouTubeSubscribeWrapper();
    youTubeSubscribeWrapper.clearCache();
```
YouTube will sometimes send multiple times the same data.
Thats why there is a cache.
The cache saves the VideoIDs from every video so nothing gets posted multiple times.
