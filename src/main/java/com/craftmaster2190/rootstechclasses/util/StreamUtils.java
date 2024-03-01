package com.craftmaster2190.rootstechclasses.util;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.stream.*;

public class StreamUtils {
  public static <T> Stream<T> stream(@Nullable Iterable<T> iterable) {
    if (iterable == null) {
      return Stream.empty();
    }
    return stream(iterable.spliterator());
  }
  public static <T> Stream<T> stream(@Nullable Iterator<T> iterator) {
    if (iterator == null) {
      return Stream.empty();
    }
    return stream(Spliterators.spliteratorUnknownSize(iterator, 0));
  }
  public static <T> Stream<T> stream(@Nullable Spliterator<T> spliterator) {
    if (spliterator == null) {
      return Stream.empty();
    }
    return StreamSupport.stream(spliterator, false);
  }
}
