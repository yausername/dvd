package org.yausername.dvd.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Returns a list with all links contained in the input
     */
    static List<String> extractUrls(@NonNull final String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);
        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }
        return containedUrls;
    }

    public static String cleanUrl(@NonNull final String url) {
        List<String> extractedUrls = Utils.extractUrls(url);
        for (String link : extractedUrls) {
            return link;
        }
        return url;
    }

}
