//  FocusExample.java
// An example of focus traversal using the keyboard to navigate through a
// small set of buttons.
//
package jcs.ui.layout.tiles.sandbox;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class FocusExample extends JFrame {

  public FocusExample() {

    super("Focus Example");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    MyPanel mypanel = new MyPanel();

    JButton button1 = new JButton("One");
    JButton button2 = new JButton("Two");
    JButton button3 = new JButton("Three");
    JButton button4 = new JButton("Four");
    JButton button5 = new MyButton("Five*");
    JButton button6 = new MyButton("Six*");
    JButton button7 = new JButton("Seven");

    mypanel.add(button2);
    mypanel.add(button3);

    JInternalFrame frame1 = new JInternalFrame("Internal Frame 1",
            true, true, true, true);

    frame1.setBackground(Color.lightGray);
    frame1.getContentPane().setLayout(new GridLayout(2, 3));
    frame1.setSize(300, 200);

    frame1.getContentPane().add(button1);
    frame1.getContentPane().add(mypanel);
    frame1.getContentPane().add(button4);
    frame1.getContentPane().add(button5);
    frame1.getContentPane().add(button6);
    frame1.getContentPane().add(button7);

    JDesktopPane desktop = new JDesktopPane();
    desktop.add(frame1, new Integer(1));
    desktop.setOpaque(true);

    //  Now set up the user interface window.
    Container contentPane = getContentPane();
    contentPane.add(desktop, BorderLayout.CENTER);
    setSize(new Dimension(400, 300));
    frame1.setVisible(true);
    setVisible(true);
  }

  public static void main(String[] args) {
    new FocusExample();
  }

  class MyButton extends JButton {

    public MyButton(String s) {
      super(s);
    }

    public boolean isFocusable() {
      return false;
    }
  }

  class MyPanel extends JPanel {

    public MyPanel() {
      super(true);
      java.util.Set upKeys = new java.util.HashSet(1);
      upKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0));
      setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS,
              upKeys);
    }

    public boolean isFocusCycleRoot() {
      return true;
    }
  }
}
