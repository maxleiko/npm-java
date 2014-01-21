package fr.braindead.npmjava.util;

import fr.braindead.npmjava.util.OSValidator;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 20/01/14
 * Time: 17:43
 */
public class Conf {
    
    public static String getNpmCachePath() {
        StringBuilder builder = new StringBuilder(System.getProperty("user.home"));
        builder.append(File.separator);
        if (OSValidator.isWindows()) {
            builder.append("AppData")
                    .append(File.separator)
                    .append("Roaming")
                    .append(File.separator)
                    .append("npm-cache")
                    .append(File.separator);
        } else {
            builder.append(".npm")
                    .append(File.separator);
        }

        builder.append("-")
                .append(File.separator)
                .append("all")
                .append(File.separator)
                .append(".cache.json");

        return builder.toString();
    }
}
