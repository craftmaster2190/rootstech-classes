package com.craftmaster2190.rootstechclasses.config;

import com.craftmaster2190.rootstechclasses.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange serverWebExchange,
                           WebFilterChain webFilterChain) {
    var stopwatch = new Stopwatch();
    var method = serverWebExchange.getRequest().getMethod();
    var path = serverWebExchange.getRequest().getPath();
    var userAgent = serverWebExchange.getRequest().getHeaders().get(HttpHeaders.USER_AGENT);
    var accept = serverWebExchange.getRequest().getHeaders().get(HttpHeaders.ACCEPT);
    var requestId = serverWebExchange.getRequest().getId();
    var requestToString = "%s %s [%s]".formatted(method, path, requestId);

    log.info(">>> {} Accept: {} User-Agent: {}", requestToString, accept, userAgent);
    return webFilterChain.filter(serverWebExchange).doFinally(ignored ->
          log.info("<<< {} Finished after: {}", requestToString, stopwatch));
  }
}
