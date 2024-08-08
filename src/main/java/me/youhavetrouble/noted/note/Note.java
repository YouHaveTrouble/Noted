package me.youhavetrouble.noted.note;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.Color;
import java.util.UUID;

public class Note {

    public final UUID id;
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

    public Note(
            @NotNull UUID id,
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
        this.id = id;
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

    public Note withTitle(@NotNull String title) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withTitleUrl(@Nullable String titleUrl) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withContent(@NotNull String content) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withImageUrl(@Nullable String imageUrl) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withThumbnailUrl(@Nullable String thumbnailUrl) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withColor(@Nullable Color color) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withAuthor(@Nullable String author) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }
    public Note withAuthorUrl(@Nullable String authorUrl) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withFooter(@Nullable String footer) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
    }

    public Note withFooterUrl(@Nullable String footerUrl) {
        return new Note(
                id,
                title,
                titleUrl,
                content,
                imageUrl,
                thumbnailUrl,
                color,
                author,
                authorUrl,
                footer,
                footerUrl
        );
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

    public static Note createNew(
            @NotNull String title,
            @NotNull String content
    ) {
        UUID id = UUID.randomUUID();
        return new Note(
                id,
                title,
                null,
                content,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
