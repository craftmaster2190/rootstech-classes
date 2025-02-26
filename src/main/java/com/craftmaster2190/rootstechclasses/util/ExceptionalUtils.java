package com.craftmaster2190.rootstechclasses.util;

import java.util.concurrent.Callable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionalUtils {
  public static <T> T invoke(Callable<T> invoke) {
    try {
      return invoke.call();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends Exception> void invoke(ExceptionalRunnable<T> invoke) {
    try {
      invoke.run();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public interface ExceptionalRunnable<T extends Exception> {
    void run() throws T;
  }
}
