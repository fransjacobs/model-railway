/*
 * Copyright (C) 2020 Frans Jacobs <frans.jacobs@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.ui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class ProcessFactory {

  private static ProcessFactory instance;

  private final List<Process> processes;

  private ProcessFactory() {
    processes = new ArrayList<>();
  }

  public static ProcessFactory getInstance() {
    if (instance == null) {
      instance = new ProcessFactory();
    }
    return instance;
  }

  public Process startJVMProces(String jvmOptions, String mainClass) {
    return startJVMProcess(jvmOptions, mainClass, null);
  }

  public Process startJVMProcess(final String jvmOptions, final String mainClass, final String[] arguments) {
    Process process = null;
    try {
      ProcessBuilder processBuilder = createProcess(jvmOptions, mainClass, arguments);

      Logger.trace("Starting process: " + mainClass + "...");
      process = processBuilder.start();

      Logger.trace("Checking process error stream...");

      BufferedReader ein = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      Logger.trace("Error stream ready: " + ein.ready());
      String line = "";
      if (ein.ready()) {
        line = ein.readLine();
        Logger.trace("ErrorStream: " + line);
      }

      //if (line.contains("Error: Could not find or load main class") || line.contains("ERROR") || line.contains("Error")) {
      if (line.contains("Error: Could not find or load main class")) {
        Logger.error("Could not start class " + mainClass + " " + line);
        process.destroyForcibly();
        return null;
      } else {
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        processes.add(process);
        Logger.debug("Started " + process.toString() + "...");
      }
    } catch (NoClassDefFoundError ncde) {
      Logger.error("Can't find class " + mainClass);
      Logger.trace(ncde);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return process;
  }

  private ProcessBuilder createProcess(final String jvmOptions, final String mainClass, final String[] arguments) {
    Logger.debug("Starting Class: " + mainClass + "...");

    String jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    Logger.trace("jvmOptions: " + jvmOptions+"\nclasspath: " + classpath+"\nmainClass: " + mainClass);

    String[] args = arguments;

    if (args == null) {
      args = new String[]{""};
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < args.length; i++) {
        sb.append(args[i]);
        if ((i + 1) < args.length) {
          sb.append(",");
        }
        Logger.trace("arguments: " + sb);
      }
    }

    String[] options = jvmOptions.split(" ");
    List< String> command = new ArrayList<>();
    command.add(jvm);
    command.addAll(Arrays.asList(options));
    command.add("-cp");
    command.add(classpath);
    command.add(mainClass);
    command.addAll(Arrays.asList(args));

    //Logger.trace("Command: " + command);

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    //Map< String, String> environment = processBuilder.environment();
    //environment.put("CLASSPATH", classpath);
    return processBuilder;
  }

  public void killProcess(final Process process) {
    process.destroy();
  }

  public List<Process> getProcesses() {
    return this.processes;
  }

  public void shutdown() {
    Logger.debug("Killing " + processes.size() + " processes.");
    processes.forEach((process) -> {
      killProcess(process);
    });
  }

}
