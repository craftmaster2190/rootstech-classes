package com.craftmaster2190.rootstechclasses.util;

import com.craftmaster2190.rootstechclasses.config.CurrentYear;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.util.TimeZone;

public class DateTimeUtils {
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE',' MMM d");
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h':'mm a");
  public static final ZoneId MST = TimeZone.getTimeZone("America/Denver")
      .toZoneId();
  public static final DateTimeFormatter WEEKDAY_MONTH_DAY_TIME_PARSER = new DateTimeFormatterBuilder()
      .appendPattern("EEE',' MMM d h':'mm a")
      .parseDefaulting(ChronoField.YEAR, CurrentYear.get())
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
      .toFormatter()
      .withZone(MST);

  public static ZonedDateTime parseDateFromCalendarItem(JsonNode json) {
    // "Date": "Sat, Mar 8",
    //    "Time": "1:30 PM",
    var date = json.at("/Date")
                   .asText() + " " + json.at("/Time")
                   .asText();

    return WEEKDAY_MONTH_DAY_TIME_PARSER.parse(date, ZonedDateTime::from);
  }

  public static ZonedDateTime parseUnixEpochMST(long dateUnixEpoch) {
    return Instant.ofEpochMilli(dateUnixEpoch).atZone(MST);
  }
}
