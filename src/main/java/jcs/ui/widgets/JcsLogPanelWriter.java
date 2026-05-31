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
package jcs.ui.widgets;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.tinylog.Logger;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.pattern.FormatPatternParser;
import org.tinylog.pattern.Token;
import org.tinylog.writers.Writer;

/**
 *
 * @author fransjacobs
 */
public class JcsLogPanelWriter extends JPanel implements Writer {

  // The instance TinyLog creates via SPI
  private static JcsLogPanelWriter instance;

  //private final String delimiter;
  private final Map<String, String> properties;
  private final StringBuilder builder;
  private final Token token;

  private static final String DEFAULT_FORMAT_PATTERN = "{date: HH:mm:ss.SSS}: {message}";
  private static final int BUILDER_CAPACITY = 1024;
  private static final String NEW_LINE = System.getProperty("line.separator");

  public JcsLogPanelWriter() {
    this(null);
  }

  public JcsLogPanelWriter(Map<String, String> properties) {
    instance = this;  // register the SPI-created instance
    this.properties = properties;

    String pattern = getStringValue("format", false);
    if (pattern == null) {
      pattern = DEFAULT_FORMAT_PATTERN;
    }

    token = new FormatPatternParser(getStringValue("exception")).parse(pattern + NEW_LINE);
    builder = getBooleanValue("writingthread") ? new StringBuilder(BUILDER_CAPACITY) : null;

    //if (properties != null) {
    //  delimiter = properties.getOrDefault("delimiter", "-");
    //} else {
    //  delimiter = "-";
    //}
    initComponents();
    postInitComponents();
  }

  private void postInitComponents() {
    logTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(()
                -> logTextArea.setCaretPosition(logTextArea.getDocument().getLength())
        );
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
      }
    });

  }

  public static JcsLogPanelWriter getInstance() {
    return instance;
  }

  @Override
  public Collection<LogEntryValue> getRequiredLogEntryValues() {
    return EnumSet.of(LogEntryValue.LEVEL, LogEntryValue.MESSAGE);
  }

  @Override
  public void write(LogEntry logEntry) {
    //if (TAG.equals(logEntry.getTag())) {
    SwingUtilities.invokeLater(() -> {
      //logTextArea.append(logEntry.getTimestamp().toDate() + " " + logEntry.getLevel() + " " + delimiter + " " + logEntry.getMessage() + "\n");

      logTextArea.append(render(logEntry));

      //Cap the log buffer, keep at most 1000 lines
      String text = logTextArea.getText();
      String[] lines = text.split("\n", -1);
      if (lines.length > 1000) {
        int cut = text.indexOf("\n") + 1;
        try {
          logTextArea.getDocument().remove(0, cut);
        } catch (BadLocationException ex) {
          Logger.error(ex.getMessage());
        }
      }

    });

    //}
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

  /**
   * Gets the trimmed value for the passed key from the configuration properties.
   *
   * <p>
   * Leading and trailing spaces of the found value will be removed.
   * </p>
   *
   * @param key Case-sensitive property key
   * @return Found value or {@code null}
   */
  private String getStringValue(final String key) {
    return getStringValue(key, true);
  }

  /**
   * Gets the string value for the passed key from the configuration properties.
   *
   * <p>
   * Leading and trailing spaces of the found value will be removed.
   * </p>
   *
   * @param key Case-sensitive property key
   * @param trimmed {@code true} for trimming the found value, otherwise {@code false}
   * @return Found value or {@code null}
   */
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

  /**
   * Gets the boolean value for the passed key from the configuration properties.
   *
   * <p>
   * Under the hood, {@link Boolean#parseBoolean(String)} is used with the trimmed string value.
   * </p>
   *
   * @param key Case-sensitive property key
   * @return Found boolean value
   */
  private boolean getBooleanValue(final String key) {
    return Boolean.parseBoolean(getStringValue(key));
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    logScrollPane = new javax.swing.JScrollPane();
    logTextArea = new javax.swing.JTextArea();

    setLayout(new java.awt.BorderLayout());

    logTextArea.setColumns(20);
    logTextArea.setRows(5);
    logTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
    logScrollPane.setViewportView(logTextArea);

    add(logScrollPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane logScrollPane;
  private javax.swing.JTextArea logTextArea;
  // End of variables declaration//GEN-END:variables
}
