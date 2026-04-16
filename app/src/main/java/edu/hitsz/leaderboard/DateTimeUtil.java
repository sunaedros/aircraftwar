package edu.hitsz.leaderboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtil {

    private static final SimpleDateFormat FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private DateTimeUtil() {
    }

    public static String now() {
        return FORMATTER.format(new Date());
    }
}
