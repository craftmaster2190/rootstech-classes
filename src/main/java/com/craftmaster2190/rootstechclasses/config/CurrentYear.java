package com.craftmaster2190.rootstechclasses.config;

import java.time.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class CurrentYear {
  private static final int ROOTSTECH_YEAR = calculateRootsTechYear();

  public static int get() {
    return ROOTSTECH_YEAR;
  }

  private static int calculateRootsTechYear() {
    var now = LocalDateTime.now();

    int year = Month.SEPTEMBER.compareTo(now.getMonth()) < 0 ? now.getYear() + 1 : now.getYear();

    log.info("Determined current RootsTech year as {}", year);
    return year;
  }

  @GetMapping("rootstech-year")
  public int fetchCurrentYear() {
    return get();
  }
}
