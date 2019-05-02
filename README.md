# RedditStream
ProjectReactor-based Reddit crawler in Java 11.

Licensed under the [MIT License](https://github.com/arudiscord/redditstream/blob/master/LICENSE).

### Installation

![Latest Version](https://api.bintray.com/packages/arudiscord/maven/redditstream/images/download.svg)

Using in Gradle:

```gradle
repositories {
  jcenter()
}

dependencies {
  compile 'pw.aru.libs:redditstream:LATEST' // replace LATEST with the version above
}
```

Using in Maven:

```xml
<repositories>
  <repository>
    <id>central</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>pw.aru.libs</groupId>
    <artifactId>redditstream</artifactId>
    <version>LATEST</version> <!-- replace LATEST with the version above -->
  </dependency>
</dependencies>
```

### Usage

The starting point of the library is the ``RedditStream`` class.

Use the ``RedditStream#stream`` method to create a stream which returns a ProjectReactor's [``Flux``](https://projectreactor.io/docs/core/release/api/index.html?reactor/core/publisher/Flux.html)

```java
for (Post post : new RedditStream().stream().toIterable()) {
    System.out.println("NEW POST: " + post.getPermalink());
}
```

### Support

Support is given on [Aru's Discord Server](https://discord.gg/URPghxg)

[![Aru's Discord Server](https://discordapp.com/api/guilds/403934661627215882/embed.png?style=banner2)](https://discord.gg/URPghxg)
