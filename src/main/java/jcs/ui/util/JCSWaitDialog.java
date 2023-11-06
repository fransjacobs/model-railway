/*
 * Copyright 2023 frans.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INPUT_VALUE_PROPERTY;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.tinylog.Logger;

public class JCSWaitDialog extends JDialog {

  private boolean canceled = false;
  private final JLabel label = new JLabel(" ");
  private final JProgressBar progressBar = new JProgressBar();

  /**
   *
   * @param parent the parent of the dialog (see {@link JDialog#JDialog(java.awt.Dialog) } for details)
   * @param dialogTitle
   * @param labelText the text of info label
   * @param progressBarText the text of the progress bar
   */
  public JCSWaitDialog(Frame parent, String dialogTitle, String labelText, String progressBarText) {
    super(parent, dialogTitle, true);
    init(labelText, progressBarText, parent);
  }

  public JCSWaitDialog(Window parent, String dialogTitle, String labelText, String progressBarText) {
    super(parent, dialogTitle, ModalityType.APPLICATION_MODAL);
    init(labelText, progressBarText, parent);
  }

  private void init(String labelText, String progressBarText, Component parent) {
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    this.label.setText(labelText);
    this.progressBar.setString(progressBarText);
    this.progressBar.setStringPainted(true);

    JOptionPane optionPane = new JOptionPane(new Object[]{label, progressBar},
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
            new String[]{"Cancel"}, null) {

      @Override
      public int getMaxCharactersPerLineCount() {
        return 60;
      }
    };

    optionPane.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (JCSWaitDialog.this.isVisible() && event.getSource() == optionPane && (event.getPropertyName().equals(VALUE_PROPERTY) || event.getPropertyName().equals(INPUT_VALUE_PROPERTY))) {
          JCSWaitDialog.this.canceled = true;
          JCSWaitDialog.this.setVisible(false);
          JCSWaitDialog.this.dispose();
        }
      }
    });
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(optionPane,
            BorderLayout.CENTER);
    pack();
    this.setLocationRelativeTo(parent);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public JLabel getLabel() {
    return label;
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  private class WorkerWaiter implements PropertyChangeListener {

    private JDialog dialog;

    public WorkerWaiter(JDialog dialog) {
      this.dialog = dialog;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if ("state".equals(event.getPropertyName()) && SwingWorker.StateValue.DONE == event.getNewValue()) {
        dialog.setVisible(false);
        dialog.dispose();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  public static void main(String[] a) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.warn("Can't set the LookAndFeel: " + ex);
    }

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(() -> {
      JCSWaitDialog dialog = new JCSWaitDialog(new javax.swing.JFrame(), "Test JCSDialog", "Dialog Label" ,"Progress");
      
      //String dialogTitle, String labelText, String progressBarText
      // TODO
      
      dialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      dialog.setLocationRelativeTo(null);
      dialog.pack();
      dialog.setVisible(true);
    });

  }

}
