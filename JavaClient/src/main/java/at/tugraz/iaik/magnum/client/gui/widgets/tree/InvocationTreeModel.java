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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import at.tugraz.iaik.magnum.client.util.DateHelper;
import at.tugraz.iaik.magnum.model.InstanceModel;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;
import at.tugraz.iaik.magnum.model.MethodModel;

public class InvocationTreeModel {
  private ConcurrentMap<String, List<DefaultMutableTreeNode>> knownMethods;

  public InvocationTreeModel(DefaultMutableTreeNode root) {
    knownMethods = new ConcurrentHashMap<String, List<DefaultMutableTreeNode>>();
  }

  public List<DefaultMutableTreeNode> getInvocationForMethod(String uniqueMethodName) {
    return knownMethods.get(uniqueMethodName);
  }

  public void addInvocation(MethodInvocationModel invocation) {
    MethodModel methodModel = invocation.getMethodModel();
    List<DefaultMutableTreeNode> invocationList = knownMethods.get(methodModel.getUniqueMethodName());
    
    if (invocationList == null) {
      invocationList = new CopyOnWriteArrayList<DefaultMutableTreeNode>();
      knownMethods.put(methodModel.getUniqueMethodName(), invocationList);
    }

    String callId = Long.toString(invocation.getCallId(), 26);
    String date = DateHelper.formatTimestamp(invocation.getInvocationTime());
    DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode(callId + " @ " + date);

    if (invocation.didReturn()) {
      if (methodModel.hasReturnValue())
        timeNode.add(new InstanceReturnTreeNode(invocation.getReturnValue()));
    } else {
      timeNode.add(new DefaultMutableTreeNode("(still running …)"));
    }

    for (InstanceModel arg : invocation.getArguments())
      timeNode.add(new InstanceArgumentTreeNode(arg));

    invocationList.add(timeNode);
  }
}
