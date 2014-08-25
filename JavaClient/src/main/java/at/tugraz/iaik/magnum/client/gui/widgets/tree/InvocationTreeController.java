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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class InvocationTreeController extends DefaultTreeModel {
  private static final long serialVersionUID = -6128457029938670188L;

  private InvocationTreeModel invocationTreeModel;
  private String selectedMethod;
  private List<DefaultMutableTreeNode> invocations;
  private DefaultMutableTreeNode rootNode;

  public InvocationTreeController(InvocationTreeModel invocationTreeModel, DefaultMutableTreeNode rootNode) {
    super(rootNode);
    this.invocationTreeModel = invocationTreeModel;
    this.rootNode = rootNode;
  }

  public void selectMethod(String uniqueMethodName) {
    selectedMethod = uniqueMethodName;
    invocations = invocationTreeModel.getInvocationForMethod(selectedMethod);

    rootNode.removeAllChildren();

    if (invocations != null) {
      rootNode.setUserObject("Invocations (" + invocations.size() + ")");
      
      for (DefaultMutableTreeNode node : invocations)
        rootNode.add(node);
    } else {
      rootNode.setUserObject("No invocations yet.");
    }

    fireTreeStructureChanged(this, new Object[] { rootNode }, new int[] { 0 }, new Object[] { rootNode });
  }
}
