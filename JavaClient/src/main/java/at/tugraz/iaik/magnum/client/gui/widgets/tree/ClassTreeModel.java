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
package at.tugraz.iaik.magnum.client.gui.widgets.tree;

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import at.tugraz.iaik.magnum.client.util.datatypes.SortedList;
import at.tugraz.iaik.magnum.model.ClassModel;

public class ClassTreeModel extends DefaultTreeModel {
  private static final long               serialVersionUID = 7814309200682676934L;

  private final DefaultMutableTreeNode    rootNode;
  private final SortedList<ClassModel>    classes;
  private final LinkedList<ClassTreeNode> childNodes;

  private final Updater                   updater;

  public ClassTreeModel(DefaultMutableTreeNode rootNode) {
    super(rootNode);
    childNodes = new LinkedList<>();
    updater = new Updater();
    this.rootNode = rootNode;
    classes = new SortedList<>();
    new Thread(updater).start();
  }

  public void addClassModel(final ClassModel classModel) {
    final ClassTreeNode classNode = new ClassTreeNode(classModel);
    synchronized (rootNode) {
      classes.add(classModel);
      childNodes.add(classNode);
      rootNode.insert(classNode, classes.indexOf(classModel));
    }

    fireTreeNodesInserted(this, new Object[] { rootNode }, new int[] { rootNode.getIndex(classNode) },
        new Object[] { classNode });
  }

  public ClassTreeModel filterBy(String needle) {
    DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("Search Results");
    ClassTreeModel filter = new ClassTreeModel(newRoot);
    Enumeration<ClassTreeNode> children = rootNode.children();

    while (children.hasMoreElements()) {
      ClassTreeNode c = children.nextElement();

      boolean match = c.getText().toLowerCase().contains(needle.toLowerCase());
      if (match) {
        newRoot.add(c.copy(true));
        continue;
      }

      Enumeration<MethodTreeNode> cChildren = c.children();
      ClassTreeNode ctn = c.copy();

      while (cChildren.hasMoreElements()) {
        MethodTreeNode child = (MethodTreeNode) cChildren.nextElement();
        if (child.getText().contains(needle.toLowerCase()))
          ctn.add(child.copy());
      }
      if (!ctn.isLeaf())
        newRoot.add(ctn);
    }
    return filter;
  }

  public void refresh() {
    synchronized (rootNode) {
      for (ClassTreeNode node : childNodes) {
        node.updateInvocations();
        node.getIcon();
        for (MethodTreeNode mNode : node.getChildren()) {
          mNode.getIcon();
          nodeChanged(mNode);
        }
        nodeChanged(node);
      }
    }
  }

  private class Updater implements Runnable {
    private Updater() {
      // Cannot be created outside
    }

    public void run() {
      while (true) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        try {
          EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
              synchronized (rootNode) {
                refresh();
              }
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
