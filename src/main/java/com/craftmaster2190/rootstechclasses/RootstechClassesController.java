package com.craftmaster2190.rootstechclasses;

import com.craftmaster2190.rootstechclasses.config.*;
import com.craftmaster2190.rootstechclasses.util.*;
import com.fasterxml.jackson.databind.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.craftmaster2190.rootstechclasses.util.JsonUtils.streamElements;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RootstechClassesController {

  private final RootstechClassesFetcher rootstechClassesFetcher;
  private final ObjectMapper objectMapper;
  private final Comparator<JsonNode> SORT = Comparator.comparing(DateTimeUtils::parseDateFromCalendarItem)
      .thenComparing(calendarItem -> calendarItem.get("Location").asText(""))
      .thenComparing(calendarItem -> calendarItem.get("Classroom").asText(""));

  private static JsonNode extractCalendarItems(JsonNode json) {
    return json.at("/data/CalendarDetail/stages/0/calendarItems");
  }

  static String asciiOnly(String str) {
    return Normalizer.normalize(str.replace((char) 0x2018, '\'')
            .replace((char) 0x2019, '\''), Normalizer.Form.NFD)
        .replaceAll("[^\\x00-\\x7F]", "");
  }

  static void addDownloadHeader(ServerHttpResponse serverResponse, String filename) {
    serverResponse.getHeaders()
        .add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s;".formatted(filename));
  }

  @GetMapping("/health")
  public void healthcheck() {
  }

  @GetMapping(value = "sessions-raw", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<JsonNode> fetchSessionsRaw() {
    return rootstechClassesFetcher.fetchSessions();
  }

  @GetMapping(value = "sessions", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<JsonNode> fetchSessions() {
    return fetchSessionsRaw()
        .map(json -> getCalendarArray(extractCalendarItems(json)));
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
        .sorted(Comparator.<JsonNode>comparingLong(calendarItem -> calendarItem.at("/date")
                .asLong())
            .thenComparing(calendarItem -> calendarItem.at("/item/classroomName")
                .asText()))
        .map(calendarItem -> {
          var dateTime = DateTimeUtils.parseUnixEpochMST(calendarItem.at("/date").asLong());

          var date = DateTimeUtils.DATE_FORMATTER.format(dateTime);
          var time = DateTimeUtils.TIME_FORMATTER.format(dateTime);
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
              .asText(null);
          var location = calendarItem.at("/item/sessionLocation")
              .asText("");

          var availableForViewingAfterConference = !(location.contains("In Person") && !location.contains("Online"));

          return JsonUtils.objectNodeOf(objectMapper, new LinkedHashMap<>() {
                {
                  put("Date", date);
                  put("Time", time);
                  put("Title", asciiOnly(title));
                  put("Speakers", asciiOnly(speakers));
                  put("Classroom", classroom);
                  put("Location", location);
                  put("Available for Viewing After Conference", availableForViewingAfterConference ? "Yes" : "No");
                  put("url", url);
                }
              }
          );
        })
        .distinct()
        .toList());
  }

  @GetMapping(value = "mainstage-raw", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<JsonNode> fetchMainStageRaw() {
    return rootstechClassesFetcher.fetchMainStageEvents();
  }

  @GetMapping(value = "mainstage", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<JsonNode> fetchMainStage() {
    return fetchMainStageRaw()
        .map(json -> getCalendarArray(extractCalendarItems(json)));
  }

  @GetMapping(
      value = { "all", "csv" },
      produces = CsvEncoder.TEXT_CSV_VALUE)
  public Mono<JsonNode> fetchAllCsv(ServerHttpResponse serverResponse) {
    addDownloadHeader(serverResponse, "RootsTech2025_Printable_Schedule.csv");
    return fetchAll();
  }

  @GetMapping(
      value = { "all", "xlsx" },
      produces = XlsxEncoder.APPLICATION_XLSX_VALUE)
  public Mono<JsonNode> fetchAllXlsx(ServerHttpResponse serverResponse) {
    addDownloadHeader(serverResponse, "RootsTech2025_Printable_Schedule.xlsx");
    return fetchAll();
  }

  @GetMapping(
      value = { "all", "json" },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<JsonNode> fetchAllJson(ServerHttpResponse serverResponse) {
    addDownloadHeader(serverResponse, "RootsTech2025_Printable_Schedule.json");
    return fetchAll();
  }

  private Mono<JsonNode> fetchAll() {
    return Mono.zip(fetchSessions(), fetchMainStage())
        .map(tuple -> {
          var duplicates = new HashSet<JsonNode>(); // Not thread-safe, do not parallelize the following lines.
          var results = Stream.of(tuple.getT1(), tuple.getT2())
              .flatMap(JsonUtils::streamElements)
              .sorted(SORT)
              .filter(json -> {
                boolean isDistinct = duplicates.add(json);
                if (!isDistinct) {
                  log.debug("Removing duplicate: {}", json);
                }
                return isDistinct;
              })
              .toList();
          return JsonUtils.arrayNodeOf(objectMapper, results);
        });
  }

}

