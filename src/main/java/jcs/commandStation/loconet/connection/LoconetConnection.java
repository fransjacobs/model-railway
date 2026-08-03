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
package jcs.commandStation.loconet.connection;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import jcs.commandStation.loconet.LoconetMessage;

/**
 *
 */
public interface LoconetConnection extends AutoCloseable {

  LoconetMessage sendMessage(LoconetMessage message);

  CompletableFuture<LoconetMessage> sendAndAwaitEcho(
          LoconetMessage message,
          Duration timeout
  );

  CompletableFuture<LoconetMessage> sendAndAwait(
          LoconetMessage message,
          Predicate<LoconetMessage> matcher,
          Duration timeout
  );

  void addMessageListener(LoconetMessageListener listener);

  void removeMessageListener(LoconetMessageListener listener);

  boolean isConnected();

  @Override
  void close();

  BlockingQueue<LoconetMessage> getMessageQueue();

}
