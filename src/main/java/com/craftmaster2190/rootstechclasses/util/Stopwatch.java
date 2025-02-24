package com.craftmaster2190.rootstechclasses.util;

import java.time.*;
import lombok.Data;

@Data
public class Stopwatch {
  private final Instant start = Instant.now();

  public Duration duration() {
    return Duration.between(start, Instant.now());
  }

  @Override
  public String toString() {
    return duration().toString().replaceFirst("PT", "");
  }
}
