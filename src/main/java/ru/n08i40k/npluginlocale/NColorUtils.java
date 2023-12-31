package ru.n08i40k.npluginlocale;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NColorUtils {
    static class PrefixMatch {
        public PrefixMatch() {}

        int begin;
        int end;

        String match;
    }

    private static List<PrefixMatch> getMatches(String message) {
        List<PrefixMatch> matches = new ArrayList<>();

        Pattern pattern = Pattern.compile("&#[a-zA-Z0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            PrefixMatch prefixMatch = new PrefixMatch();

            prefixMatch.match = matcher.group(0).substring(1);
            prefixMatch.begin = matcher.start(0);
            prefixMatch.end = matcher.end(0);

            matches.add(prefixMatch);
        }

        return matches;
    }

    public static String removeDuplicatedTags(String message) {
        List<PrefixMatch> matches = getMatches(message);

        for (int i = matches.size() - 1; i > 0; --i) {
            PrefixMatch currentMatch = matches.get(i);
            PrefixMatch previousMatch = matches.get(i - 1);

            if (!currentMatch.match.equals(previousMatch.match))
                continue;

            message = message.substring(0, currentMatch.begin) + message.substring(currentMatch.end);
        }

        return message;
    }

    public static TextComponent translate(String message, boolean italic) {
        message = message.replace("&r", "&#ffffff");
        message = ChatColor.translateAlternateColorCodes('&', message);

        message = removeDuplicatedTags(message);

        List<String> textParts = new ArrayList<>();
        List<String> textPrefixes = new ArrayList<>();

        List<PrefixMatch> matches = getMatches(message);
        if (matches.isEmpty()) {
            textPrefixes.add("#ffffff");
            textParts.add(message);
        } else {
            if (matches.get(0).begin > 0)
                textPrefixes.add("#ffffff");

            int offset = 0;

            for (PrefixMatch match : matches) {
                if (match.begin > offset) {
                    textPrefixes.add(match.match);
                    textParts.add(message.substring(offset).substring(0, match.begin - offset));
                } else {
                    if (match.begin == 0)
                        textPrefixes.add(match.match);
                    else
                        textPrefixes.set(textPrefixes.size() - 1, match.match);
                }
                offset = match.end;
            }

            if (message.length() >= offset)
                textParts.add(message.substring(offset));
        }


        List<TextComponent> componentList = new ArrayList<>();

        for (int i = 0; i < textParts.size(); ++i) {
            if (textParts.get(i).isEmpty()) continue;

            componentList.add(
                    Component.text(textParts.get(i))
                            .color(TextColor.fromHexString(
                                    textPrefixes.get(i)
                            )));
        }

        Component component = Component.empty().children(componentList);

        if (!italic)
            component = component.decoration(TextDecoration.ITALIC, false);

        return (TextComponent) component;
    }

    public static String generateGradientString(String message, String hexStart, String hexEnd) {
        List<String> gradient = generateHexGradient(hexStart, hexEnd, message.length());

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < message.length(); ++i) {
            result.append("&").append(gradient.get(i)).append(message.charAt(i));
        }

        return result.toString();
    }

    public static List<String> generateHexGradient(String startHex, String endHex, int size) {
        List<String> gradient = new ArrayList<>();

        if (size <= 1) {
            gradient.add("#" + startHex);
            return gradient;
        }

        // Convert the start and end hex codes to RGB values
        int startRed = Integer.parseInt(startHex.substring(0, 2), 16);
        int startGreen = Integer.parseInt(startHex.substring(2, 4), 16);
        int startBlue = Integer.parseInt(startHex.substring(4, 6), 16);

        int endRed = Integer.parseInt(endHex.substring(0, 2), 16);
        int endGreen = Integer.parseInt(endHex.substring(2, 4), 16);
        int endBlue = Integer.parseInt(endHex.substring(4, 6), 16);

        // Calculate the step size for each color component
        int redStep = (endRed - startRed) / (size - 1);
        int greenStep = (endGreen - startGreen) / (size - 1);
        int blueStep = (endBlue - startBlue) / (size - 1);

        // Generate the gradient by interpolating between the start and end colors
        for (int i = 0; i < size; i++) {
            int red = startRed + (redStep * i);
            int green = startGreen + (greenStep * i);
            int blue = startBlue + (blueStep * i);

            // Convert the RGB values back to hex code
            String hexCode = String.format("#%02X%02X%02X", red, green, blue);
            gradient.add(hexCode);
        }

        gradient.set(gradient.size() - 1, "#" + endHex);

        return gradient;
    }

    public static class ColorTagMatch {
        public ColorTagMatch() {}

        @Getter
        int begin;
        int end;

        boolean isClosing;

        String text;
    }

    public static List<ColorTagMatch> getGradientTags(String message, String regex, boolean isClosing) {
        final Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        final Matcher matcher = pattern.matcher(message);

        List<ColorTagMatch> tags = new ArrayList<>();

        while (matcher.find()) {
            ColorTagMatch tag = new ColorTagMatch();

            tag.text = matcher.group(1);
            tag.begin = matcher.start(0);
            tag.end = matcher.end(0);
            tag.isClosing = isClosing;

            tags.add(tag);
        }

        return tags;
    }

    public static String parseGradientTags(String message) {
        List<ColorTagMatch> startTags = getGradientTags(message, "<#([0-9a-fA-F]{6})>", false);
        List<ColorTagMatch> endTags = getGradientTags(message, "</#([0-9a-fA-F]{6})>", true);

        List<ColorTagMatch> tags = new ArrayList<>();
        tags.addAll(startTags);
        tags.addAll(endTags);

        tags.sort(Comparator.comparingInt(ColorTagMatch::getBegin));

        StringBuilder result = new StringBuilder();

        boolean isOpened = false;

        if (tags.isEmpty()) {
            result.append(message);
            return result.toString();
        }

        if (tags.get(0).begin != 0)
            result.append(message, 0, tags.get(0).begin);


        for (int i = 0; i < tags.size(); ++i) {
            ColorTagMatch tag = tags.get(i);

            if (i + 1 >= tags.size() && !tag.isClosing)
                return "No closing tag found!";

            if (tag.isClosing) {
                isOpened = false;

                if (i + 1 >= tags.size()) {
                    result.append(message.substring(tag.end));
                    break;
                } else {
                    ColorTagMatch nextTag = tags.get(i + 1);
                    String textBetween = message.substring(tag.end, nextTag.begin);

                    result.append(textBetween);
                }
                continue;
            }

            if (!isOpened)
                isOpened = true;

            ColorTagMatch nextTag = tags.get(i + 1);

            String textBetween = message.substring(tag.end, nextTag.begin);

            result.append(generateGradientString(textBetween, tag.text, nextTag.text));
        }

        return result.toString();
    }
}