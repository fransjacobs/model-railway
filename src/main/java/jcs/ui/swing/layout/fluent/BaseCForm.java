/*
* Copyright 2018 Frederik Wiers
*
* Permission is hereby granted, free of charge, to any person obtaining a 
* copy of this software and associated documentation files (the 
* "Software"), to deal in the Software without restriction, including 
* without limitation the rights to use, copy, modify, merge, publish, 
* distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to 
* the following conditions:
*
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package jcs.ui.swing.layout.fluent;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for building a form. With this class a leaf of the container tree is active and can be changed by adding a child or
 * going up the tree.
 *
 * @author fred
 *
 */
public class BaseCForm<CFORM extends BaseCForm<CFORM, CSIZE, CTYPE>, CSIZE extends BaseCSize<CSIZE, CTYPE>, CTYPE extends Component> {

  private List<Container> ctree = new ArrayList<Container>();
  private CSIZE cs;
  private CTYPE lastComponent;

  @SuppressWarnings("unchecked")
  protected CFORM me() {
    return (CFORM) this;
  }

  /**
   * @param ctree The new container tree (set directly, no copy is made).
   */
  public CFORM setContainerList(List<Container> ctree) {
    this.ctree = ctree;
    return me();
  }

  /**
   * @return a shallow copy of the container tree.
   */
  public List<Container> getContainerList() {
    return new ArrayList<Container>(ctree);
  }

  public CFORM setCSize(CSIZE cs) {
    this.cs = cs;
    return me();
  }

  /**
   * Returns the component sizer.
   * <br>See also {@link #csize()}
   */
  public CSIZE getCSize() {
    return cs;
  }

  /**
   * Returns the component sizer with the last component set.
   */
  public CSIZE csize() {
    return cs.set(getLast());
  }

  /**
   * Clears the container tree. To keep the root, use {@link #toRoot()}.
   */
  public CFORM clear() {
    ctree.clear();
    return me();
  }

  /**
   * Removes all containers from the container tree except root. Same as {@link #toIndex(int)} with index 0.
   */
  public CFORM toRoot() {
    return toIndex(0);
  }

  /**
   * Clears the container tree and sets given container as root container, unless given root is null.
   */
  public CFORM setRoot(Container root) {

    ctree.clear();
    if (root != null) {
      ctree.add(root);
    }
    return me();
  }

  /**
   * Returns the number of containers in the container tree.
   */
  public int size() {
    return ctree.size();
  }

  /**
   * Returns the container at the top of the container tree. Same as {@link #get(int)} with index 0.
   */
  public Container getRoot() {
    return get(0);
  }

  /**
   * Returns the current active container.
   */
  public Container get() {
    return get(size() - 1);
  }

  /**
   * Returns the container at the given index. Index 0 will return root container.
   */
  public Container get(int index) {
    return ctree.get(index);
  }

  /**
   * Add a component to the current active container. Last component is set to given component.
   * <br>Use {@link #addChild(Container)} to add a container to the container tree.
   * <br>See also {@link #csize()} which uses the last component.
   */
  public CFORM add(CTYPE c) {
    get().add(setLast(c).getLast());
    return me();
  }

  /**
   * Sets last component to given component. See also {@link #add(Component)}.
   */
  public CFORM setLast(CTYPE c) {
    lastComponent = c;
    return me();
  }

  /**
   * Returns the component that was last added to a container.
   * <br>See also {@link #csize()}
   */
  public CTYPE getLast() {
    return lastComponent;
  }

  /**
   * Add a child-container to the current container (if any). The child container becomes the active current container.
   * <br>Use {@link #add(Component)} to add a component to the current container.
   */
  public CFORM addChild(Container c) {

    if (size() > 0) {
      get().add(c);
    }
    ctree.add(c);
    return me();
  }

  /**
   * True if current active container is the root container ({@link #size()} is 1).
   */
  public boolean atRoot() {
    return (size() == 1);
  }

  /**
   * Set active container to the parent of the current container. Current container is removed from container tree.
   *
   * @throws IndexOutOfBoundsException when current active container is the root container.
   */
  public CFORM up() {
    // First see if we go beyond root, throws exception if that happens.
    ctree.get(size() - 2);
    // Then remove
    ctree.remove(size() - 1);
    return me();
  }

  /**
   * Moves the current active container to the given index (0 is root). Any containers after the given index are removed from the
   * container tree.
   *
   * @throws IndexOutOfBoundsException when index is not available.
   */
  public CFORM toIndex(int index) {

    ctree.get(index);
    while (size() - 1 > index) {
      ctree.remove(size() - 1);
    }
    return me();
  }

  /**
   * Inserts the specfied container at the index (0 is root). Does nothing if given container is null.
   *
   * @throws IndexOutOfBoundsException when index is not reachable.
   */
  public CFORM insert(int index, Container c) {

    if (c != null) {
      ctree.add(index, c);
    }
    return me();
  }
}
