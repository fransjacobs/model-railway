/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.ui.widgets.shuttle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

/**
 *
 * @author frans
 * @param <T>
 */
public class Shuttle<T> extends JPanel {

  private static final long serialVersionUID = 5383882478992457817L;

  private final SortedListModel<T> sourceListModel;
  private final SortedListModel<T> destListModel;

  /**
   * Creates new form Shuttle
   */
  public Shuttle() {
    sourceListModel = new SortedListModel<>();
    destListModel = new SortedListModel<>();

    initComponents();
  }

  public String getSourceChoicesTitle() {
    return sourceLabel.getText();
  }

  public void setSourceTitle(String title) {
    sourceLabel.setText(title);
  }

  public String getDestinationTitle() {
    return this.destinationLabel.getText();
  }

  public void setDestinationTitle(String newValue) {
    destinationLabel.setText(newValue);
  }

  public void clearSourceListModel() {
    sourceListModel.clear();
  }

  public void clearDestinationListModel() {
    destListModel.clear();
  }

  public void addSourceElements(ListModel<T> newValue) {
    fillListModel(sourceListModel, newValue);
  }

  public void setSourceElements(ListModel<T> newValue) {
    clearSourceListModel();
    addSourceElements(newValue);
  }

  public void addDestinationElements(ListModel<T> newValue) {
    fillListModel(destListModel, newValue);
  }

  private void fillListModel(SortedListModel<T> model, ListModel<T> newValues) {
    int size = newValues.getSize();
    for (int i = 0; i < size; i++) {
      model.add(newValues.getElementAt(i));
    }
  }

  public void addSourceElements(T newValue[]) {
    fillListModel(sourceListModel, newValue);
  }

  public void addSourceElements(List<T> newValues) {
    sourceListModel.addAll(newValues);
  }

  public void setSourceElements(T newValue[]) {
    clearSourceListModel();
    addSourceElements(newValue);
  }

  public void setSourceElements(List<T> newValues) {
    clearSourceListModel();
    addSourceElements(newValues);
  }

  public void addDestinationElements(T newValue[]) {
    fillListModel(destListModel, newValue);
  }

  public void addDestinationElements(List<T> newValues) {
    destListModel.addAll(newValues);
  }

  private void fillListModel(SortedListModel model, T newValues[]) {
    model.addAll(newValues);
  }

  public Iterator sourceIterator() {
    return sourceListModel.iterator();
  }

  public Iterator destinationIterator() {
    return destListModel.iterator();
  }

  public void setSourceCellRenderer(ListCellRenderer newValue) {
    sourceList.setCellRenderer(newValue);
  }

  public ListCellRenderer getSourceCellRenderer() {
    return sourceList.getCellRenderer();
  }

  public void setDestinationCellRenderer(ListCellRenderer newValue) {
    destinationList.setCellRenderer(newValue);
  }

  public ListCellRenderer getDestinationCellRenderer() {
    return destinationList.getCellRenderer();
  }

  public void setVisibleRowCount(int newValue) {
    sourceList.setVisibleRowCount(newValue);
    destinationList.setVisibleRowCount(newValue);
  }

  public int getVisibleRowCount() {
    return sourceList.getVisibleRowCount();
  }

  public void setSelectionBackground(Color newValue) {
    sourceList.setSelectionBackground(newValue);
    destinationList.setSelectionBackground(newValue);
  }

  public Color getSelectionBackground() {
    return sourceList.getSelectionBackground();
  }

  public void setSelectionForeground(Color newValue) {
    sourceList.setSelectionForeground(newValue);
    destinationList.setSelectionForeground(newValue);
  }

  public Color getSelectionForeground() {
    return sourceList.getSelectionForeground();
  }

  private void clearSourceSelected() {
    List<T> svl = sourceList.getSelectedValuesList();
    for (int i = 0; i < svl.size(); i++) {
      sourceListModel.removeElement(svl.get(i));
    }

//    Object selected[] = sourceList.getSelectedValues();
//    for (int i = selected.length - 1; i >= 0; --i) {
//      sourceListModel.removeElement(selected[i]);
//    }
    sourceList.getSelectionModel().clearSelection();
  }

  private void clearDestinationSelected() {
    List<T> svl = destinationList.getSelectedValuesList();
    for (int i = 0; i < svl.size(); i++) {
      destListModel.removeElement(svl.get(i));
    }

//    Object selected[] = destinationList.getSelectedValues();
//    for (int i = selected.length - 1; i >= 0; --i) {
//      destListModel.removeElement(selected[i]);
//    }
    destinationList.getSelectionModel().clearSelection();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
   * method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    GridBagConstraints gridBagConstraints;

    sourceScrollPane = new JScrollPane();
    sourceList = new JList<T>(sourceListModel);
    addButton = new JButton();
    removeButton = new JButton();
    destinationScrollPane = new JScrollPane();
    destinationList = new JList<T>(destListModel);
    sourceLabel = new JLabel();
    destinationLabel = new JLabel();

    setMinimumSize(new Dimension(205, 100));
    setLayout(new GridBagLayout());

    sourceScrollPane.setAutoscrolls(true);
    sourceScrollPane.setMaximumSize(new Dimension(40, 80));
    sourceScrollPane.setMinimumSize(new Dimension(40, 80));
    sourceScrollPane.setPreferredSize(new Dimension(40, 80));
    sourceScrollPane.setRequestFocusEnabled(false);
    sourceScrollPane.setRowHeaderView(null);
    sourceScrollPane.setViewportView(sourceList);

    sourceList.setMaximumSize(new Dimension(40, 80));
    sourceList.setMinimumSize(new Dimension(40, 80));
    sourceList.setPreferredSize(new Dimension(40, 80));
    sourceScrollPane.setViewportView(sourceList);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(0, 5, 5, 0);
    add(sourceScrollPane, gridBagConstraints);

    addButton.setIcon(new ImageIcon(getClass().getResource("/media/select-right-24.png"))); // NOI18N
    addButton.setToolTipText("");
    addButton.setActionCommand("add");
    addButton.setMaximumSize(new Dimension(40, 40));
    addButton.setMinimumSize(new Dimension(40, 40));
    addButton.setPreferredSize(new Dimension(40, 40));
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        addButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.ipady = 4;
    gridBagConstraints.insets = new Insets(30, 5, 5, 0);
    add(addButton, gridBagConstraints);

    removeButton.setIcon(new ImageIcon(getClass().getResource("/media/select-left-24.png"))); // NOI18N
    removeButton.setMaximumSize(new Dimension(40, 40));
    removeButton.setMinimumSize(new Dimension(40, 40));
    removeButton.setPreferredSize(new Dimension(40, 40));
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.ipady = 4;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(removeButton, gridBagConstraints);

    destinationScrollPane.setAutoscrolls(true);
    destinationScrollPane.setMaximumSize(new Dimension(40, 80));
    destinationScrollPane.setMinimumSize(new Dimension(40, 80));
    destinationScrollPane.setPreferredSize(new Dimension(40, 80));
    destinationScrollPane.setRowHeaderView(null);
    destinationScrollPane.setViewportView(destinationList);

    destinationList.setMaximumSize(new Dimension(40, 80));
    destinationList.setMinimumSize(new Dimension(40, 80));
    destinationList.setPreferredSize(new Dimension(40, 80));
    destinationScrollPane.setViewportView(destinationList);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(0, 5, 5, 5);
    add(destinationScrollPane, gridBagConstraints);

    sourceLabel.setText("Accesoires");
    sourceLabel.setToolTipText("Available");
    sourceLabel.setMaximumSize(new Dimension(100, 16));
    sourceLabel.setMinimumSize(new Dimension(100, 16));
    sourceLabel.setPreferredSize(new Dimension(100, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.ipadx = 5;
    gridBagConstraints.ipady = 10;
    gridBagConstraints.insets = new Insets(2, 7, 0, 0);
    add(sourceLabel, gridBagConstraints);

    destinationLabel.setText("Selected");
    destinationLabel.setMaximumSize(new Dimension(100, 16));
    destinationLabel.setMinimumSize(new Dimension(100, 16));
    destinationLabel.setPreferredSize(new Dimension(100, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.ipadx = 8;
    gridBagConstraints.ipady = 10;
    gridBagConstraints.insets = new Insets(2, 7, 0, 0);
    add(destinationLabel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void addButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    List<T> svl = sourceList.getSelectedValuesList();
   addDestinationElements(svl);
    clearSourceSelected();
  }//GEN-LAST:event_addButtonActionPerformed

  private void removeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    List<T> svl = destinationList.getSelectedValuesList();
    addSourceElements(svl);
    clearDestinationSelected();
  }//GEN-LAST:event_removeButtonActionPerformed

  class SortedListModel<T> extends AbstractListModel<T> {

    private static final long serialVersionUID = 4953952860580655243L;

    private final SortedSet<T> model;

    public SortedListModel() {
      model = new TreeSet<>();
    }

    @Override
    public int getSize() {
      return model.size();
    }

    @Override
    public T getElementAt(int index) {
      return (T) model.toArray()[index];
    }

    public void add(T element) {
      if (model.add(element)) {
        fireContentsChanged(this, 0, getSize());
      }
    }

    public void addAll(T elements[]) {
      Collection<T> c = Arrays.asList(elements);
      model.addAll(c);
      fireContentsChanged(this, 0, getSize());
    }

    public void addAll(Collection<T> elements) {
      model.addAll(elements);
      fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
      model.clear();
      fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(T element) {
      return model.contains(element);
    }

    public T firstElement() {
      return model.first();
    }

    public Iterator<T> iterator() {
      return model.iterator();
    }

    public T lastElement() {
      return model.last();
    }

    public boolean removeElement(T element) {
      boolean removed = model.remove(element);
      if (removed) {
        fireContentsChanged(this, 0, getSize());
      }
      return removed;
    }
  }

  public static void main(String args[]) {
    JFrame f = new JFrame("Dual List Box Tester");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Shuttle<String> shuttle = new Shuttle<>();
    shuttle.addSourceElements(new String[]{"One", "Two", "Three"});
    shuttle.addSourceElements(new String[]{"Four", "Five", "Six"});
    shuttle.addSourceElements(new String[]{"Seven", "Eight", "Nine"});
    shuttle.addSourceElements(new String[]{"Ten", "Eleven", "Twelve"});
    shuttle.addSourceElements(new String[]{"Thirteen", "Fourteen", "Fifteen"});
    shuttle.addSourceElements(new String[]{"Sixteen", "Seventeen", "Eighteen"});
    shuttle.addSourceElements(new String[]{"Nineteen", "Twenty", "Thirty"});
    f.getContentPane().add(shuttle, BorderLayout.CENTER);
    f.setSize(400, 300);
    f.setVisible(true);
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton addButton;
  private JLabel destinationLabel;
  private JList<T> destinationList;
  private JScrollPane destinationScrollPane;
  private JButton removeButton;
  private JLabel sourceLabel;
  private JList<T> sourceList;
  private JScrollPane sourceScrollPane;
  // End of variables declaration//GEN-END:variables
}
