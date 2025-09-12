/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.util;

import org.tinylog.Logger;

/**
 * Trace class to log calling methods. used for debugging and profiling
 *
 */
public class Tracer {

  /**
   * This is the method of interest. We want to know who is calling it. It's marked as synchronized to prevent the log output from different threads from mixing together and becoming unreadable.
   */
  public static synchronized void methodThatGetsCalled() {
    //System.out.println("--- methodThatGetsCalled() has been invoked! ---");

    // Get the current thread so we can inspect its properties.
    Thread currentThread = Thread.currentThread();

    // The thread's name is extremely useful. Always name your threads!
    System.out.println("Invoked by Thread: '" + currentThread.getName() + "' (ID: " + currentThread.threadId() + ")");

    // getStackTrace() returns an array of StackTraceElement objects.
    // This array represents the call stack at this exact moment.
    StackTraceElement[] stackTrace = currentThread.getStackTrace();

    // How to read the stack trace array:
    // stackTrace[0] is always getStackTrace() itself.
    // stackTrace[1] is the current method (methodThatGetsCalled).
    // stackTrace[2] is the method that CALLED our method. This is what we want!
    // stackTrace[3] is the method that called the caller, and so on.
    if (stackTrace.length > 2) {
      StackTraceElement caller = stackTrace[2];
      System.out.println("Caller ==> " + caller.getClassName() + "." + caller.getMethodName()
              + " (at " + caller.getFileName() + ":" + caller.getLineNumber() + ")");
    }

    System.out.println("-------------------------------------------------\n");
  }

}
