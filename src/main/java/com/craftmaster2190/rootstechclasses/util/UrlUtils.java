package com.craftmaster2190.rootstechclasses.util;

import java.net.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtils {

  public static boolean isValidUrl(String url) {
    if (url == null) {
      return false;
    }

    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      return false;
    }

    if (!url.contains(".")) { // Yes, there are urls like "http://localhost", but for our purposes they are invalid.
      return false;
    }

    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }
}
