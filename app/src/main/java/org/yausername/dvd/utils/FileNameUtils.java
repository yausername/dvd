package org.yausername.dvd.utils;

public class FileNameUtils {

    public static String createFilename(String title) {
        String cleanFileName = title.replaceAll("[\\\\><\"|*?'%:#/]", " ");
        String fileName = cleanFileName.trim().replaceAll(" +", " ");
        if (fileName.length() > 127)
            fileName = fileName.substring(0, 127);

        return fileName;
    }

}
