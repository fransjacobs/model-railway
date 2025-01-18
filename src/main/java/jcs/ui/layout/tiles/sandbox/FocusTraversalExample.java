//FocusTraversalExample.java
// Similar to the FocusExample, this class uses the custom AlphaButtonPolicy
// focus traversal policy to navigate another small set of buttons.
//
package jcs.ui.layout.tiles.sandbox;

import java.awt.*;
import javax.swing.*;

public class FocusTraversalExample extends JPanel {

  public FocusTraversalExample() {
    setLayout(new GridLayout(6, 1));
    JButton button1 = new JButton("Texas");
    JButton button2 = new JButton("Vermont");
    JButton button3 = new JButton("Florida");
    JButton button4 = new JButton("Alabama");
    JButton button5 = new JButton("Minnesota");
    JButton button6 = new JButton("California");

    setBackground(Color.lightGray);
    add(button1);
    add(button2);
    add(button3);
    add(button4);
    add(button5);
    add(button6);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Alphabetized Button Focus Traversal");
    frame.setFocusTraversalPolicy(new AlphaButtonPolicy());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(new FocusTraversalExample());
    frame.setSize(400, 300);
    frame.setVisible(true);
  }
}
