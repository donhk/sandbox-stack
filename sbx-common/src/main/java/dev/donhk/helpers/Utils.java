package dev.donhk.helpers;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String resource2txt(String resourceName) throws IOException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        if (classLoader.getResource(resourceName) == null) {
            throw new IOException("Resource not found " + resourceName);
        }
        InputStream in = classLoader.getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * @param resourceName resource file name
     * @param key          key to look for
     * @return string value of what was found
     */
    public static String rStr(String resourceName, String key) {
        return ResourceBundle.getBundle(resourceName).getString(key);
    }

    /**
     * @param resourceName resource file name
     * @param key          key to look for
     * @return int value of what was found
     */
    public static int rInt(String resourceName, String key) {
        return Integer.parseInt(ResourceBundle.getBundle(resourceName).getString(key));
    }

    /**
     * @param key key to look for
     * @return string value of what was found
     */
    public static String rStr(String key) {
        return rStr("appconfig", key);
    }

    /**
     * @param key key to look for
     * @return int value of what was found
     */
    public static int rInt(String key) {
        return rInt("appconfig", key);
    }

    public static List<String> deserializeList(String input) {
        if (!validateSerializedList(input)) {
            return null;
        }
        List<String> response = new ArrayList<>();
        String[] paths = input.replaceAll("[\\[\\]]", "").split(",");
        for (String path : paths) {
            response.add(path.trim());
        }
        return response;
    }

    public static boolean validateSerializedList(String input) {
        if (input == null) {
            return false;
        }
        // [/path/to/file, /path/to/file, /path/to/file]
        Pattern pattern = Pattern.compile("\\[([a-zA-Z]:)?([\\\\/a-z\\sA-Z0-9_.-]+)(,\\s*([a-zA-Z]:)?([\\\\/a-z\\sA-Z0-9_.-]+))*]");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static String base64Encode(String source) {
        return Base64.getEncoder().encodeToString(source.getBytes());
    }

    public static String base64Decode(String source) {
        byte[] decodedBytes = Base64.getDecoder().decode(source);
        return new String(decodedBytes);
    }

    /**
     * Create MD% of a given text
     *
     * @param source source text to create the digest from
     * @return MD5 string
     */
    public static String digest(String source) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "NA" + System.currentTimeMillis();
        }
        md.update(source.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return (sb.toString());
    }

    /**
     * Kill machine process from terminal
     *
     * @param machineName machine to kill
     * @param logger      logger instance
     */
    public static void killVMHardWay(String machineName, Logger logger) {
        final String cmd = "kill -9 `ps aux | grep " + machineName + " | grep -v grep | awk '{print $2}'`";
        logger.info("killing machine externally with " + cmd);
        final ProcessBuilder pb = new ProcessBuilder(
                "sh",
                "-c",
                cmd
        );
        try {
            pb.start();
            pb.wait(100L);
        } catch (IOException ioe) {
            logger.warn(ioe.getMessage(), ioe);
        } catch (InterruptedException e) {
            logger.info("command [" + cmd + "] was interrupted");
        } catch (Exception e) {
            logger.warn("Unexpected exception " + e.getMessage(), e);
        }

    }
}
