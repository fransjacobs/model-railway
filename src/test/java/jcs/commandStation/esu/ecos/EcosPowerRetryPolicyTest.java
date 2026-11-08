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

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class EcosPowerRetryPolicyTest {

  @Test
  void retriesOnlyCode32AndOnlyWithinLimit() {
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long id = policy.beginRequest();
    assertTrue(policy.shouldRetryGo(id, 32, 1, 2));
    assertFalse(policy.shouldRetryGo(id, 0, 1, 2));
    assertFalse(policy.shouldRetryGo(id, 25, 1, 2));
    assertFalse(policy.shouldRetryGo(id, 32, 2, 2));
  }

  @Test
  void actualEcosError32ReplyIsRecognized() {
    EcosMessage message = new EcosMessage("set(1, status[GO])");
    message.addResponse("<REPLY set(1, status[GO])>\\n<END 32 (no go after stop)>\\n");
    assertTrue(message.isResponseComplete());
    assertEquals(EcosPowerRetryPolicy.NO_GO_AFTER_STOP, message.getErrorCode());
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long request = policy.beginRequest();
    assertTrue(policy.shouldRetryGo(request, message.getErrorCode(), 1, 2));
  }

  @Test
  void laterStopCancelsPriorGo() {
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long go = policy.beginRequest();
    long stop = policy.beginRequest();
    assertFalse(policy.isCurrent(go));
    assertTrue(policy.isCurrent(stop));
    assertFalse(policy.shouldRetryGo(go, 32, 1, 2));
  }

  @Test
  void disconnectCancelsPendingGo() {
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long go = policy.beginRequest();
    policy.cancelPending();
    assertFalse(policy.isCurrent(go));
  }

  @Test
  void stopInterruptsCooldownWithoutWaitingFullDelay() throws Exception {
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long go = policy.beginRequest();
    CountDownLatch started = new CountDownLatch(1);
    AtomicBoolean retried = new AtomicBoolean(true);
    Thread worker = new Thread(() -> {
      started.countDown();
      try {
        retried.set(policy.awaitRetry(go, 2_000));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    worker.start();
    assertTrue(started.await(1, TimeUnit.SECONDS));
    policy.beginRequest(); // STOP arrives on a different worker
    worker.join(500);
    assertFalse(worker.isAlive(), "GO retry must not block STOP");
    assertFalse(retried.get(), "Cancelled GO must not be sent");
  }

  @Test
  void uninterruptedWaitAllowsRetry() throws Exception {
    EcosPowerRetryPolicy policy = new EcosPowerRetryPolicy();
    long go = policy.beginRequest();
    assertTrue(policy.awaitRetry(go, 1));
  }
}
