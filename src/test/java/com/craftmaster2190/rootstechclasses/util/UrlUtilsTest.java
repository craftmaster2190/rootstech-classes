package com.craftmaster2190.rootstechclasses.util;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilsTest {

  @Test
  void isValidUrl() {
    assertThatPossibleUrlIsValidUrl(null, false);
    assertThatPossibleUrlIsValidUrl("", false);
    assertThatPossibleUrlIsValidUrl(" ", false);
    assertThatPossibleUrlIsValidUrl("foo", false);
    assertThatPossibleUrlIsValidUrl("http://", false);
    assertThatPossibleUrlIsValidUrl("//google.com", false);
    assertThatPossibleUrlIsValidUrl("google.com", false);
    assertThatPossibleUrlIsValidUrl("http://google.com", true);
    assertThatPossibleUrlIsValidUrl("https://google.com", true);
    assertThatPossibleUrlIsValidUrl("foobar://www.familysearch.org", false);
    assertThatPossibleUrlIsValidUrl("http://www.familysearch.org/", true);
    assertThatPossibleUrlIsValidUrl("https://www.familysearch.org", true);
    assertThatPossibleUrlIsValidUrl("https://www.familysearch.org/", true);
    assertThatPossibleUrlIsValidUrl("https://www.familysearch.org/rootstech/session/finding-your-world-war-i-or-wwii-soldier", true);
    assertThatPossibleUrlIsValidUrl("tel://8015551234", false);
    assertThatPossibleUrlIsValidUrl("ftp://www.familysearch.org/", false);
  }

  private static void assertThatPossibleUrlIsValidUrl(String possibleUrl, boolean expectIsValidUrl) {
    assertThat(UrlUtils.isValidUrl(possibleUrl))
        .describedAs("Expecting %s to be %s url", possibleUrl, (expectIsValidUrl ? "valid" : "invalid"))
        .isEqualTo(expectIsValidUrl);
  }
}