package org.yausername.dvd.utils

import java.util.ArrayList
import java.util.regex.Pattern

object Utils {
    /**
     * Returns a list with all links contained in the input
     */
    private fun extractUrls(text: String): List<String> {
        val containedUrls: MutableList<String> = ArrayList()
        val urlRegex =
            "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val urlMatcher = pattern.matcher(text)
        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)))
        }
        return containedUrls
    }

    fun cleanUrl(url: String): String {
        val extractedUrls = extractUrls(url)
        for (link in extractedUrls) {
            return link
        }
        return url
    }

}