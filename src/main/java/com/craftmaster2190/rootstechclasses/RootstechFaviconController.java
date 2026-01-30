package com.craftmaster2190.rootstechclasses;

import com.craftmaster2190.rootstechclasses.util.LazyValueSupplier;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RootstechFaviconController {

  private final WebClient webClient;
  private final Supplier<Mono<byte[]>> cachedFavicon = new LazyValueSupplier<>(() -> fetchFavicon().cache());

  @GetMapping("/favicon.ico")
  public Mono<byte[]> favicon() {
    return cachedFavicon.get();
  }

  // https://www.familysearch.org/en/rootstech/
  // /html/head/link[1]
  // <link href="https://edge.fscdn.org/assets/docs/fs_logo_favicon_sq.png" rel="icon" type="image/x-icon">
  public Mono<byte[]> fetchFavicon() {
    return webClient.get()
        .uri("https://www.familysearch.org/en/rootstech/")
        .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_XML_VALUE)
        .header(HttpHeaders.USER_AGENT,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0")
        .header("Host", "www.familysearch.org")
        .header("Referer", "https://www.familysearch.org/")
        .header("Origin", "https://www.familysearch.org/")
        .retrieve()
        .bodyToMono(String.class)
        .map(htmlString -> {
              var rootstechHtmlDoc = Jsoup.parse(htmlString);
              var faviconLinks = rootstechHtmlDoc.selectXpath("/html/head/link[@rel='icon']");
              var faviconLink = faviconLinks.get(0);
              return faviconLink.attr("href");
            }
        )
        .flatMap(faviconUrl -> webClient.get()
            .uri(faviconUrl)
            // image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5
            .header(HttpHeaders.ACCEPT,
                "image/avif",
                "image/webp",
                MediaType.IMAGE_PNG_VALUE,
                "image/svg+xml",
                "image/*")
            .header(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0")
            .header("Host", "edge.fscdn.org")
            .header("Referer", "https://www.familysearch.org/")
            .retrieve()
            .bodyToMono(byte[].class)
        );
  }

}
