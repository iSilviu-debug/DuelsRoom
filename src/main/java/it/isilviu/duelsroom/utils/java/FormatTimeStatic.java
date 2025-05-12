package it.isilviu.duelsroom.utils.java;

import javax.annotation.Nullable;

public class FormatTimeStatic {

    /**
     * Format time in a specific format.
     * @param format The format type, can be
     *               "HH:mm:ss", "Hh mm'm' ss's'"
     * @param millis The time in milliseconds
     * @return The formatted time as a String
     */
    public static String formatTime(@Nullable String format, long millis) {
        long totalSeconds = millis / 1000;
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long hours = totalMinutes / 60;


        String result = format == null ? "HH:mm:ss" : format;

        if (result.equals("smart")) {
            StringBuilder sb = new StringBuilder();
            if (hours > 0) {
                sb.append(String.format("%d:%02d:%02d", hours, minutes, seconds));
            } else if (minutes > 0) {
                sb.append(String.format("%d:%02d", minutes, seconds));
            } else {
                sb.append(seconds);
            }

            return sb.toString().trim();
        }

        result = result.replace("HH", String.format("%02d", hours));
        result = result.replace("H", String.valueOf(hours));
        result = result.replace("mm", String.format("%02d", minutes));
        result = result.replace("m", String.valueOf(minutes));
        result = result.replace("ss", String.format("%02d", seconds));
        result = result.replace("s", String.valueOf(seconds));

        return result;
    }
}
