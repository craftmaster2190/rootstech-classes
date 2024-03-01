package com.craftmaster2190.rootstechclasses;

import com.craftmaster2190.rootstechclasses.util.JsonUtils;
import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.craftmaster2190.rootstechclasses.util.JsonUtils.streamElements;

@RestController
@RequiredArgsConstructor
public class RootstechClassesController {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE',' MMM d h':'mm a");
  private final RootstechClassesFetcher rootstechClassesFetcher;
  private final ObjectMapper objectMapper;

  private static JsonNode extractCalendarItems(JsonNode json, String stageTitle) {
    return streamElements(json.at("/data/CalendarDetail/stages"))
        .filter(jsonStage -> Objects.equals(jsonStage.get("title")
            .asText(), stageTitle))
        .findAny()
        .orElseThrow(() -> new IllegalStateException("No `" + stageTitle + "` stages.title element"))
        .get("calendarItems");
  }

  @GetMapping("sessions-raw")
  public Mono<JsonNode> fetchSessionsRaw() {
    return rootstechClassesFetcher.fetchSessions();
  }

  @GetMapping("sessions")
  public Mono<JsonNode> fetchSessions() {
    return fetchSessionsRaw()
        .map(json -> getCalendarArray(extractCalendarItems(json, "Sessions")));
  }

  private JsonNode getCalendarArray(JsonNode calendarItems) {
    return JsonUtils.arrayNodeOf(objectMapper, streamElements(calendarItems)
        .filter(calendarItem -> {
          if (calendarItem == null) {
            return false;
          }
          JsonNode item = calendarItem.at("/item");
          return item == null || !item.isEmpty();
        })
        .sorted(Comparator.comparingLong(calendarItem -> calendarItem.at("/item/date")
            .asLong()))
        .map(calendarItem -> {
          var time = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(calendarItem.at("/item/date")
                  .asLong())
              .atZone(TimeZone.getTimeZone("America/Denver")
                  .toZoneId()));
          var title = calendarItem.at("/item/title")
              .asText();
          var speakers = streamElements(calendarItem.at("/item/creators")).map(
                  creatorJson -> creatorJson.get("name")
                      .asText())
              .sorted()
              .collect(Collectors.joining(", "));
          var url = calendarItem.at("/item/url")
              .asText();
          var classroom = calendarItem.at("/item/classroomName")
              .asText();
          var location = calendarItem.at("/item/sessionLocation")
              .asText();

          var availableForViewingAfterConference = location.contains("Online")
              || classroom.equals("Ballroom H")
              || classroom.equals("Ballroom E")
              || classroom.equals("Ballroom B")
              || classroom.equals("Hall E");

          return JsonUtils.objectNodeOf(objectMapper, new LinkedHashMap<>() {
                {
                  put("time", time);
                  put("title", title);
                  put("speakers", speakers);
                  put("classroom", classroom);
                  put("location", location);
                  put("url", url);
                  put("availableForViewingAfterConference", availableForViewingAfterConference ? "Yes" : "No");
                }
              }
          );
        })
        .toList());
  }

  @GetMapping("mainstage-raw")
  public Mono<JsonNode> fetchMainStageRaw() {
    return rootstechClassesFetcher.fetchMainStageEvents();
  }

  @GetMapping("mainstage")
  public Mono<JsonNode> fetchMainStage() {
    return fetchMainStageRaw()
        .map(json -> getCalendarArray(extractCalendarItems(json, "2024")));
  }

  @GetMapping(value = "all")
  public Mono<JsonNode> fetchAll() {
    return Mono.zip(fetchSessions(), fetchMainStage())
        .map(tuple -> JsonUtils.arrayNodeOf(objectMapper, Stream.of(tuple.getT1(), tuple.getT2())
            .flatMap(JsonUtils::streamElements)
            .toList()));
  }

}

