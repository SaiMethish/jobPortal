package com.example.jobPortal.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class IndiaLocationUtil {

    private static final Pattern BENGALURU = Pattern.compile("(?i)\\b(bengaluru|bangalore|blr)\\b");
    private static final Pattern HYDERABAD = Pattern.compile("(?i)\\b(hyderabad|secunderabad|hyd)\\b");
    private static final Pattern PUNE = Pattern.compile("(?i)\\b(pune)\\b");
    private static final Pattern NCR = Pattern.compile("(?i)\\b(delhi|new delhi|ncr|gurgaon|gurugram|noida|greater noida|faridabad)\\b");
    private static final Pattern MUMBAI = Pattern.compile("(?i)\\b(mumbai|navi mumbai|thane|bombay)\\b");
    private static final Pattern CHENNAI = Pattern.compile("(?i)\\b(chennai|madras)\\b");
    private static final Pattern REMOTE_INDIA = Pattern.compile("(?i)\\b(remote\\s*[-–,]?\\s*india|india\\s*[-–,]?\\s*remote)\\b");
    private static final Pattern GENERIC_INDIA = Pattern.compile("(?i)\\b(india|in)\\b");

    /**
     * Checks if a location string corresponds to an Indian location.
     * Returns the normalized city/region name, or null if not an Indian location.
     */
    public static String extractIndianCity(String rawLocation) {
        if (rawLocation == null || rawLocation.trim().isEmpty()) {
            return null;
        }

        String loc = rawLocation.trim();

        if (REMOTE_INDIA.matcher(loc).find()) {
            return "Remote - India";
        }
        if (BENGALURU.matcher(loc).find()) {
            return "Bengaluru";
        }
        if (HYDERABAD.matcher(loc).find()) {
            return "Hyderabad";
        }
        if (PUNE.matcher(loc).find()) {
            return "Pune";
        }
        if (NCR.matcher(loc).find()) {
            return "Delhi NCR";
        }
        if (MUMBAI.matcher(loc).find()) {
            return "Mumbai";
        }
        if (CHENNAI.matcher(loc).find()) {
            return "Chennai";
        }
        if (GENERIC_INDIA.matcher(loc).find()) {
            // Also check for remote
            if (loc.toLowerCase().contains("remote")) {
                return "Remote - India";
            }
            return "India";
        }

        return null;
    }

    public static boolean isIndianLocation(String rawLocation) {
        return extractIndianCity(rawLocation) != null;
    }
}
