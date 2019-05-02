package pw.aru.libs.redditstream;

import org.json.JSONObject;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Reactor-based Reddit crawler who streams the regularly-crawled {@link Post}s to a {@link Flux}.
 */
public class RedditStream {
    public static final String USER_AGENT = "Java/RedditStream (" + System.getProperty("os.name") + ")";
    private static final AtomicInteger COUNT = new AtomicInteger();
    private final long delay;
    private final TimeUnit timeUnit;
    HttpClient client = HttpClient.newHttpClient();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory());

    /**
     * Creates a crawler with a crawl delay of 5 seconds.
     */
    public RedditStream() {
        this(5, TimeUnit.SECONDS);
    }

    /**
     * Creates a crawler with the defined crawl delay.
     *
     * @param delay    the delay amount.
     * @param timeUnit the delay time unit.
     */
    public RedditStream(long delay, TimeUnit timeUnit) {
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    private static ThreadFactory threadFactory() {
        String s = "redditstream-" + COUNT.getAndIncrement();
        return r -> new Thread(r, s);
    }

    /**
     * Starts a crawler of the specified subreddits.
     *
     * @param subreddits the subreddits to filter, or empty to don't filter.
     * @return the flux of posts.
     */
    public Flux<Post> stream(String... subreddits) {
        return Flux.create(sink -> {
            var allowedSubreddits = Set.of(subreddits);
            var subredditMap = new LinkedHashMap<String, Subreddit>();
            var entries = new LinkedHashSet<String>();
            final ScheduledFuture<?> schedule = executor.scheduleAtFixedRate(() -> {
                try {
                    String body = client.send(
                        HttpRequest.newBuilder()
                            .uri(URI.create("https://www.reddit.com/r/all/new.json?limit=100"))
                            .header("User-Agent", RedditStream.USER_AGENT)
                            .build(),
                        HttpResponse.BodyHandlers.ofString()
                    ).body();
                    for (var o : new JSONObject(body).getJSONObject("data").getJSONArray("children")) {
                        var json = ((JSONObject) o).getJSONObject("data");
                        var permalink = "https://www.reddit.com" + json.getString("permalink");
                        if (!entries.add(permalink)) return;
                        var subredditName = json.getString("subreddit");
                        if (!allowedSubreddits.isEmpty() && !allowedSubreddits.contains(subredditName)) return;
                        var subreddit = subredditMap.computeIfAbsent(subredditName, v -> new Subreddit(this, v));
                        sink.next(
                            new Post(
                                this,
                                subreddit,
                                json.getString("author"),
                                json.getString("title"),
                                json.getString("selftext"),
                                json.getString("url"),
                                permalink,
                                json.getInt("score"),
                                json.getInt("ups"),
                                json.getInt("downs"),
                                json.getBoolean("over_18"),
                                json.getBoolean("spoiler"),
                                json.getBoolean("quarantine"),
                                json.getLong("created")
                            )
                        );
                    }
                } catch (InterruptedException e) {
                    sink.complete();
                } catch (Exception e) {
                    sink.error(e);
                }
            }, 0, delay, timeUnit);
            sink.onDispose(() -> schedule.cancel(true));
        });
    }
}
