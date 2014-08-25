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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


public class ClassTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = 169766670593209378L;

  @Override
  public Component getTreeCellRendererComponent(JTree arg0, Object node, boolean arg2, boolean arg3, boolean arg4,
      int arg5, boolean arg6) {
    
    if (node instanceof ClassTreeDefaultNode)
      return ((ClassTreeDefaultNode) node).getIcon();
    else
      return super.getTreeCellRendererComponent(arg0, node, arg2, arg3, arg4, arg5, arg6);
  }  
}
