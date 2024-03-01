package com.craftmaster2190.rootstechclasses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RootstechClassesFetcher {


  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final Mono<JsonNode> cachedSessionsCall;
  private final Mono<JsonNode> cachedMainStageEventsCall;

  public RootstechClassesFetcher(WebClient webClient, ObjectMapper objectMapper) {
    this.webClient = webClient;
    this.objectMapper = objectMapper;
    cachedSessionsCall = webClientCall(
        "https://cms-z.api.familysearch.org/rootstech/api/graphql/delivery/conference?operationName=CalendarDetail&variables=%7B%22profileImage_crop%22%3Atrue%2C%22profileImage_height%22%3A250%2C%22profileImage_width%22%3A250%2C%22promoImage_crop%22%3Afalse%2C%22promoImage_height%22%3A288%2C%22promoImage_width%22%3A512%2C%22thumbnailImage_crop%22%3Afalse%2C%22thumbnailImage_height%22%3A288%2C%22thumbnailImage_width%22%3A512%2C%22id%22%3A%22%2Fcalendar%2Fsessions%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%223d0e63fb4f8bf14ed5c2c5e58c50e283e8b05e4adc29bcad93007b8ecb75eb9b%22%7D%7D");
    cachedMainStageEventsCall = webClientCall(
        "https://cms-z.api.familysearch.org/rootstech/api/graphql/delivery/conference?operationName=CalendarDetail&variables=%7B%22profileImage_crop%22%3Atrue%2C%22profileImage_height%22%3A250%2C%22profileImage_width%22%3A250%2C%22promoImage_crop%22%3Afalse%2C%22promoImage_height%22%3A288%2C%22promoImage_width%22%3A512%2C%22thumbnailImage_crop%22%3Afalse%2C%22thumbnailImage_height%22%3A288%2C%22thumbnailImage_width%22%3A512%2C%22id%22%3A%22%2Fcalendar%2Fmain-stage%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%223d0e63fb4f8bf14ed5c2c5e58c50e283e8b05e4adc29bcad93007b8ecb75eb9b%22%7D%7D");
  }

  private Mono<JsonNode> webClientCall(String graphqlUrl) {
    return this.webClient.get()
        .uri(URI.create(graphqlUrl))
        .header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.USER_AGENT,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0")
        .header("x-api-key", "kktOgVTWL3yBprDpE8TDKGzAG49GXETaf3MUOuq")
        .header("Host", "cms-z.api.familysearch.org")
        .header("Referer", "https://www.familysearch.org/")
        .header("Origin", "https://www.familysearch.org/")
        .exchangeToMono(clientResponse -> {
          var requestHeaders = clientResponse.request()
              .getHeaders()
              .toString();

          var mediaType = clientResponse.headers()
              .contentType();
          var contentLength = clientResponse.headers()
              .contentLength();

          var responseHeaders = clientResponse.headers()
              .asHttpHeaders()
              .toString();

          return clientResponse.bodyToMono(String.class)
              .doOnNext(str ->
                  str.toString());
        })
        //        .bodyToMono(String.class)
        .doOnNext(i -> log.info("Fetched at {}", Instant.now()))
        //        .cache(Duration.of(1, MINUTES))
        .flatMap((str) -> {
          try {
            return Mono.just(this.objectMapper.readTree(str));
          }
          catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Unable to parse: " + str, e));
          }
        });
  }

  public Mono<JsonNode> fetchSessions() {
    return cachedSessionsCall;
  }
  public Mono<JsonNode> fetchMainStageEvents() {
    return cachedMainStageEventsCall;
  }

  @PostConstruct
  public void initCache() {
    log.info("init");
    fetchSessions().block();
  }
}
