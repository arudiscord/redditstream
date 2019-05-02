package pw.aru.libs.redditstream;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Getter
public class Post {
    private final RedditStream redditStream;
    private Subreddit subreddit;
    private String author;
    private String title;
    private String selfText;
    private String link;
    private String permalink;
    private boolean spoiler;
    private boolean nsfw;
    private boolean quarantine;
    private int score;
    private int upvotes;
    private int downvotes;
    private long createdAt;

    public Post(RedditStream redditStream, String permalink) {
        this.redditStream = redditStream;
        this.permalink = permalink;
        update();
    }

    public Post(RedditStream redditStream, Subreddit subreddit, String author,
                String title, String selfText, String link, String permalink,
                int score, int upvotes, int downvotes,
                boolean spoiler, boolean nsfw, boolean quarantine, long createdAt
    ) {
        this.redditStream = redditStream;
        this.subreddit = subreddit;
        this.author = author;
        this.title = title;
        this.selfText = selfText;
        this.link = link;
        this.permalink = permalink;
        this.score = score;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.quarantine = quarantine;
        this.createdAt = createdAt;
    }

    public Post update() {
        try {
            String body = redditStream.client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(permalink + ".json"))
                    .header("User-Agent", RedditStream.USER_AGENT)
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();

            JSONObject json = new JSONArray(body).getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");
            author = json.getString("author");
            title = json.getString("title");
            selfText = json.getString("selftext");
            link = json.getString("url");
            score = json.getInt("score");
            upvotes = json.getInt("ups");
            downvotes = json.getInt("downs");
            nsfw = json.getBoolean("over_18");
            spoiler = json.getBoolean("spoiler");
            quarantine = json.getBoolean("quarantine");
            createdAt = json.getLong("created");
        } catch (InterruptedException e) {
            throw new RuntimeException("Operation was interrupted.", e);
        } catch (IOException e) {
            throw new UncheckedIOException("A IO exception ocurred while updating the object.", e);
        }
        return this;
    }

}
