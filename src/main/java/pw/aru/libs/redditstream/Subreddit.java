package pw.aru.libs.redditstream;

import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Getter
public class Subreddit {
    private final RedditStream redditStream;
    private String name;
    private String title;
    private String description;
    private boolean nsfw;
    private boolean quarantine;
    private int subscribers;
    private long createdAt;

    public Subreddit(RedditStream redditStream, String name) {
        this.redditStream = redditStream;
        this.name = name;
        update();
    }

    public Subreddit(RedditStream redditStream, String name, String title, String description,
                     boolean nsfw, boolean quarantine, int subscribers, long createdAt
    ) {
        this.redditStream = redditStream;
        this.name = name;
        this.title = title;
        this.description = description;
        this.nsfw = nsfw;
        this.quarantine = quarantine;
        this.subscribers = subscribers;
        this.createdAt = createdAt;
    }

    public Subreddit update() {
        try {
            String body = redditStream.client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://www.reddit.com/r/" + name + "/about.json"))
                    .header("User-Agent", RedditStream.USER_AGENT)
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();

            JSONObject json = new JSONObject(body).getJSONObject("data");

            name = json.getString("display_name");
            title = json.getString("title");
            description = json.getString("public_description");
            nsfw = json.getBoolean("over_18");
            quarantine = json.getBoolean("quarantine");
            subscribers = json.getInt("subscribers");
            createdAt = json.getLong("created");
        } catch (InterruptedException e) {
            throw new RuntimeException("Operation was interrupted.", e);
        } catch (IOException e) {
            throw new UncheckedIOException("A IO exception ocurred while updating the object.", e);
        }
        return this;
    }
}
