/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prünster
 * Copyright 2013, 2014 Bernd Prünster
 *
 *     This file is part of Magnum PI.
 *
 *     Magnum PI is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Magnum PI is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Magnum PI.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.iaik.magnum.client.cg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import at.tugraz.iaik.magnum.client.gui.widgets.trace.InvocationRenderer;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;

public class CallGraphNode implements Serializable {
  private static final long              serialVersionUID = -900045100367823462L;

  private SortedSet<DBInvocation>        invocations;
  private long                           id;
  private CallGraphNode                  caller;
  private final SortedSet<CallGraphNode> callees;

  private boolean                        selected;

  private boolean                        partofPath       = false;

  private JComponent                     component;
  private JComboBox<DBInvocation>        cBox;

  private boolean                        individual       = false;

  public CallGraphNode(DBInvocation invocation, long id) {
    this.invocations = Collections.synchronizedSortedSet(new TreeSet<DBInvocation>(
        new InvocationComparator<DBInvocation>()));
    if (invocation != null)
      invocations.add(invocation);
    this.id = id;
    callees = Collections.synchronizedSortedSet(new TreeSet<CallGraphNode>(new NodeComparator<CallGraphNode>()));

  }

  public CallGraphNode setCaller(CallGraphNode caller) {
    this.caller = caller;
    return caller.addCallee(this);
  }

  public CallGraphNode addCallee(CallGraphNode child) {
    if (!child.individual) {
      String mn = child.invocations.first().getUniqueMethodName();
      for (CallGraphNode node : callees) {
        if (node.invocations.first().getUniqueMethodName().equals(mn)) {
          node.invocations.addAll(child.invocations);
          return node;
        }
      }

    }
    callees.add(child);
    return child;
  }

  public void removeCallee(CallGraphNode child) {
    if (child != null)
      callees.remove(child);

  }

  public void removeCaller() {
    caller = null;
  }

  @Override
  public String toString() {
    if (invocations.isEmpty())
      return "<missing>";
    else
      return invocations.first().getClassName() + "." + invocations.first().getMethodName();
  }

  public long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof CallGraphNode))
      return false;

    return id == (((CallGraphNode) other).id);
  }

  /*
   * public boolean hasCaller() { return caller != null; }
   */

  public SortedSet<CallGraphNode> getCallees() {
    return callees;
  }

  public CallGraphNode getCaller() {
    return caller;
  }

  public SortedSet<DBInvocation> getInvocations() {
    return invocations;
  }

  public long removeInvocation(DBInvocation invocation) {
    invocations.remove(invocation);
    if (id != invocations.first().getId()) {
      id = invocations.first().getId();
    }
    return id;
  }

  public void setInvocation(DBInvocation invocation) {
    if (id != invocation.getId())
      throw new IllegalArgumentException("FUBAR!");
    invocations.add(invocation);
  }

  public boolean isSelected() {
    return selected;
  }

  public boolean isPartofPath() {
    return partofPath;
  }

  public void setPartofPath(boolean partofPath) {
    this.partofPath = partofPath;
  }

  public void setIndividual(boolean individual) {
    this.individual = individual;
  }

  public boolean isIndividual() {
    return individual;
  }

  public void select() {
    selected = true;
    if (component != null)
      if (selected)
        component.setBackground(new Color(200, 200, 255));
      else
        component.setBackground(Color.WHITE);

  }

  public void deselect() {
    selected = false;
    partofPath = false;
    if (component != null)
      if (selected)
        component.setBackground(new Color(200, 200, 255));
      else
        component.setBackground(Color.WHITE);
  }

  public JComponent getComponent() {
    if (((invocations.size() < 2) && (cBox == null)) || (invocations.size() >= 2 && cBox != null))
      if ((component != null))
        return component;
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    if (invocations.size() < 2) {
      if (invocations.isEmpty())
        component = new JLabel("<missing>");
      else {
        component = new JPanel(new BorderLayout());
        component.add(new JLabel(invocations.first().getClassName(), SwingConstants.LEFT), BorderLayout.NORTH);
        component.add(new JLabel(invocations.first().getMethodName(), SwingConstants.RIGHT), BorderLayout.CENTER);
        // FontMetrics fm = component.getFontMetrics(font);

      }
      // int tWidth = (int) fm.getStringBounds(((JLabel) component).getText(),
      // component.getGraphics()).getWidth();

      // component.setMaximumSize(new Dimension(width, 20));
      // component.setPreferredSize(new Dimension(width, 20));
    } else {
      component = new JPanel(new BorderLayout());
      component.add(new JLabel(invocations.first().getClassName()), BorderLayout.NORTH);
      component.add(new JLabel(invocations.first().getMethodName()), BorderLayout.CENTER);

      cBox = new JComboBox<DBInvocation>(invocations.toArray(new DBInvocation[invocations.size()]));
      cBox.setRenderer(new InvocationRenderer<DBInvocation>());
      component.add(cBox, BorderLayout.SOUTH);
    }

    component.setFont(font);
    component.setOpaque(true);
    if (selected)
      component.setBackground(new Color(180, 180, 255));
    else
      component.setBackground(Color.WHITE);
    component.setBorder(new LineBorder(Color.DARK_GRAY));

    return component;
  }

  public JComboBox<DBInvocation> getComboBox() {
    return cBox;
  }

}
