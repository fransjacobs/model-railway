/*
 * Copyright 2026 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.commandStation.esu.ecos;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serializes the *intent* of power requests, without holding the TCP lock across the ECoS STOP-to-GO cooldown.<br>
 * A later STOP cancels an older GO.
 */
final class EcosPowerRetryPolicy {

  static final int NO_GO_AFTER_STOP = 32;
  private final AtomicLong sequence = new AtomicLong();

  long beginRequest() {
    return sequence.incrementAndGet();
  }

  void cancelPending() {
    sequence.incrementAndGet();
  }

  boolean isCurrent(long request) {
    return sequence.get() == request;
  }

  boolean shouldRetryGo(long request, int responseCode, int attempt, int maxAttempts) {
    return isCurrent(request) && responseCode == NO_GO_AFTER_STOP
            && attempt > 0 && attempt < maxAttempts;
  }

  /**
   * Wait in short increments so STOP/disconnect can cancel a pending GO. Does not hold any power-command or socket lock.
   */
  boolean awaitRetry(long request, long delayMillis) throws InterruptedException {
    if (delayMillis < 0) {
      throw new IllegalArgumentException("Negative retry delay");
    }
    long remainingNanos = TimeUnit.MILLISECONDS.toNanos(delayMillis);
    long end = System.nanoTime() + remainingNanos;
    while (isCurrent(request)) {
      remainingNanos = end - System.nanoTime();
      if (remainingNanos <= 0) {
        return true;
      }
      TimeUnit.NANOSECONDS.sleep(Math.min(remainingNanos, TimeUnit.MILLISECONDS.toNanos(25)));
    }
    return false;
  }
}
