package com.craftmaster2190.rootstechclasses.config;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import org.reactivestreams.Publisher;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Flux;

@Configuration
public class WebClientProvider {

  @Bean
  public WebClient webClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs()
                .maxInMemorySize(
                    Math.toIntExact(DataSize.ofMegabytes(100)
                        .toBytes())
                ))
            .build())
        .build();
  }
}
