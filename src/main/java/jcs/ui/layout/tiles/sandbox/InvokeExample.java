// InvokeExample.java
// This class demonstrates several examples of how to handle long-running
// tasks (such as querying a remote resource).  Some of the examples are
// good, some are not!
//

package jcs.ui.layout.tiles.sandbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InvokeExample {
  private static JButton good = new JButton("Good");
  private static JButton bad = new JButton("Bad");
  private static JButton bad2 = new JButton("Bad2");
  private static JLabel resultLabel = new JLabel("Ready", JLabel.CENTER);

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Layout . . .
    JPanel p = new JPanel();
    p.setOpaque(true);
    p.setLayout(new FlowLayout());
    p.add(good);
    p.add(bad);
    p.add(bad2);
    
    Container c = f.getContentPane();
    c.setLayout(new BorderLayout());
    c.add(p, BorderLayout.CENTER);
    c.add(resultLabel, BorderLayout.SOUTH);

    // Listeners
    good.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        resultLabel.setText("Working . . .");
        setEnabled(false);

        // We're going to do something that takes a long time, so we
        // spin off a thread and update the display when we're done.
        Thread worker = new Thread() {
          public void run() {
            // Something that takes a long time . . . in real life, this
            // might be a DB query, remote method invocation, etc.
            try {
              Thread.sleep(5000);
            }
            catch (InterruptedException ex) {}

            // Report the result using invokeLater().
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                resultLabel.setText("Ready");
                setEnabled(true);
              }
            });
          }
        };

        worker.start(); // So we don't hold up the dispatch thread.
      }
    });

    bad.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        resultLabel.setText("Working . . .");
        setEnabled(false);

        // We're going to do the same thing, but not in a separate thread.
        try {
          Thread.sleep(5000); // Dispatch thread is starving!
        }
        catch (InterruptedException ex) {}

        // Report the result.
        resultLabel.setText("Ready");
        setEnabled(true);
      }
    });

    bad2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        resultLabel.setText("Working . . . ");
        setEnabled(false);

        // The wrong way to use invokeLater().  The runnable() shouldn't
        // starve the dispatch thread.
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              Thread.sleep(5000); // Dispatch thread is starving!
            }
            catch (InterruptedException ex) {}

            resultLabel.setText("Ready");
            setEnabled(true);
          }
        });
      }
    });

    f.setSize(300, 100);
    f.setVisible(true);
  }

  // Allows us to turn the buttons on or off while we work.
  static void setEnabled(boolean b) {
    good.setEnabled(b);
    bad.setEnabled(b);
    bad2.setEnabled(b);
  }
}
