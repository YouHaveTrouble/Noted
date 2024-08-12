package me.youhavetrouble.noted;

public class MessageUtil {

    public static String fixNewlines(String input) {
        return input.replaceAll("\\\\n", "\n");
    }

    public static String formatForDiscord(String string) {
        return fixNewlines(string);
    }

}
