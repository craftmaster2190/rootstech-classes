package com.craftmaster2190.rootstechclasses.util;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LazyValueSupplier<T> implements Supplier<T> {

  private final ReentrantLock lock = new ReentrantLock();
  private final Supplier<T> delegate;
  private boolean cached = false;
  private T value;

  public LazyValueSupplier(Supplier<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public T get() {
    if (!cached) {
      lock.lock();
      try {
        if (!cached) {
          value = delegate.get();
          cached = true;
        }
      } finally {
        lock.unlock();
      }
    }
    return value;
  }


}
