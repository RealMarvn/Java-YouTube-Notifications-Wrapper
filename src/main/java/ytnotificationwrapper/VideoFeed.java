package ytnotificationwrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Builder
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoFeed {
    private Entry entry;
    private boolean newVideo;

}

@Builder
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class Entry {
    private String channelId;
    private String videoId;
    private String title;
    private String link;
    private Author author;
    private Date datePublished;
    private Date dateUpdated;
}

@Builder
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class Author {
    String name;
    String uri;
}
