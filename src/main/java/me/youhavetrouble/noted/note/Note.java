package me.youhavetrouble.noted.note;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.Color;
import java.time.Duration;

public class Note {

    public static final Cache<String, Note> cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();

    public final String title;
    public final String titleUrl;
    public final String content;
    public final String imageUrl;
    public final String thumbnailUrl;
    public final Color color;
    public final String author;
    public final String authorUrl;
    public final String footer;
    public final String footerUrl;

    private Note(
            @NotNull String title,
            @Nullable String titleUrl,
            @NotNull String content,
            @Nullable String imageUrl,
            @Nullable String thumbnailUrl,
            @Nullable Color color,
            @Nullable String author,
            @Nullable String authorUrl,
            @Nullable String footer,
            @Nullable String footerUrl
    ) {
        this.title = title;
        this.titleUrl = titleUrl;
        this.content = content;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.color = color;
        this.author = author;
        this.authorUrl = authorUrl;
        this.footer = footer;
        this.footerUrl = footerUrl;
    }

    public MessageEmbed toEmbed() {
        return new EmbedBuilder()
                .setTitle(title, titleUrl)
                .setDescription(content)
                .setImage(imageUrl)
                .setThumbnail(thumbnailUrl)
                .setAuthor(author, authorUrl)
                .setFooter(footer, footerUrl)
                .setColor(color)
                .build();
    }

}
