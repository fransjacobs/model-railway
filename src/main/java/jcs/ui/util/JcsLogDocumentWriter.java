/*
 * Copyright 2026 Frans Jacobs
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
package jcs.ui.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.tinylog.Logger;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.pattern.FormatPatternParser;
import org.tinylog.pattern.Token;
import org.tinylog.writers.Writer;

/**
 *
 */
public class JcsLogDocumentWriter extends PlainDocument implements Writer {

  private final Map<String, String> properties;
  private final StringBuilder builder;
  private final Token token;

  public static final String DEFAULT_FORMAT_PATTERN = "{date: HH:mm:ss.SSS}: {message}";
  public static final int BUILDER_CAPACITY = 1024;
  public static final String NEW_LINE = System.getProperty("line.separator");

  private static JcsLogDocumentWriter instance;

  public JcsLogDocumentWriter(Map<String, String> properties) {
    instance = this;  // register the SPI-created instance
    this.properties = properties;

    String pattern = getStringValue("format", false);
    if (pattern == null) {
      pattern = DEFAULT_FORMAT_PATTERN;
    }

    token = new FormatPatternParser(getStringValue("exception")).parse(pattern + NEW_LINE);
    builder = getBooleanValue("writingthread") ? new StringBuilder(BUILDER_CAPACITY) : null;
  }

  public static JcsLogDocumentWriter getInstance() {
    return instance;
  }

  @Override
  public Collection<LogEntryValue> getRequiredLogEntryValues() {
    return EnumSet.of(LogEntryValue.LEVEL, LogEntryValue.MESSAGE);
  }

  @Override
  public void write(LogEntry logEntry) {
    SwingUtilities.invokeLater(() -> {
      try {
        insertString(0, render(logEntry), null);
      } catch (BadLocationException ex) {
        Logger.error(ex.getMessage());
      }
    });
  }

  private String render(final LogEntry logEntry) {
    if (builder == null) {
      StringBuilder sbuilder = new StringBuilder(BUILDER_CAPACITY);
      token.render(logEntry, sbuilder);
      return sbuilder.toString();
    } else {
      builder.setLength(0);
      token.render(logEntry, builder);
      return builder.toString();
    }
  }

  @Override
  public void flush() {
    SwingUtilities.invokeLater(() -> {
      try {
        //Cap the log buffer, keep at most 1000 lines
        String text = getText(0, getLength());
        String[] lines = text.split("\n", -1);
        if (lines.length > 1000) {
          int cut = text.indexOf("\n") + 1;
          remove(0, cut);
        }
      } catch (BadLocationException ex) {
        Logger.error(ex.getMessage());
      }
    });
  }

  @Override
  public void close() {
    SwingUtilities.invokeLater(() -> {
      try {
        remove(0, getLength());
      } catch (BadLocationException ex) {
        Logger.error(ex.getMessage());
      }
    });
  }

  private String getStringValue(final String key) {
    return getStringValue(key, true);
  }

  private String getStringValue(final String key, final boolean trimmed) {
    String value = properties.get(key);
    if (value == null) {
      return null;
    } else if (trimmed) {
      return value.trim();
    } else {
      return value;
    }
  }

  private boolean getBooleanValue(final String key) {
    return Boolean.parseBoolean(getStringValue(key));
  }

}
