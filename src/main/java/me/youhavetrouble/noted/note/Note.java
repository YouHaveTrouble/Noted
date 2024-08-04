package me.youhavetrouble.noted.note;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.Color;

public record Note(
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
