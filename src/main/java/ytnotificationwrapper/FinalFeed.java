package ytnotificationwrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalFeed {
    private Entry entry;
    private String channelId;
    private String videoId;
    private String title;
    private String link;
    private Author author;
    private Instant datePublished;
    private Instant dateUpdated;

    @Setter
    private boolean newVideo;
}

