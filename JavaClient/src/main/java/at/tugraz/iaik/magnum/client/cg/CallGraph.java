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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;

public class CallGraph implements Serializable {
  private static final long        serialVersionUID = -8992526582075277376L;

  private Map<Long, CallGraphNode> nodes;
  private Set<Long>                selectedIDs;

  public CallGraph() {
    nodes = Collections.synchronizedMap(new HashMap<Long, CallGraphNode>());
    selectedIDs = Collections.synchronizedSet(new HashSet<Long>());
  }

  public void clear() {
    nodes.clear();
  }

  public void addCallRelation(long from, DBInvocation fromInvocation, long to, DBInvocation toInvocation) {
    if (!nodes.containsKey(from))
      nodes.put(from, new CallGraphNode(fromInvocation, from));
    CallGraphNode nFrom = nodes.get(from);
    if (!nodes.containsKey(to))
      nodes.put(to, new CallGraphNode(toInvocation, to));
    CallGraphNode nTo = nodes.get(to);

    nTo = nFrom.addCallee(nTo);
    nodes.put(toInvocation.getId(), nTo);
    nTo.setCaller(nFrom);
    nodes.put(toInvocation.getId(), nTo);
  }

  public int getNodeCount() {
    return nodes.size();
  }

  public Map<Long, CallGraphNode> getNodes() {
    return nodes;
  }

  public void update(long id, CallGraphNode node) {
    nodes.put(id, node);
  }

  public void addCallRelation(long fromId, long toId, DBInvocation invocation, boolean individual) {
    if (!nodes.containsKey(fromId))
      nodes.put(fromId, new CallGraphNode(null, fromId));
    CallGraphNode nFrom = nodes.get(fromId);
    nFrom.setIndividual(individual);
    if (!nodes.containsKey(toId))
      nodes.put(toId, new CallGraphNode(invocation, toId));
    CallGraphNode nTo = nodes.get(toId);

    nFrom.addCallee(nTo);
    nTo.setCaller(nFrom);

  }

  public void removeCallRelation(long from, long to) {
    if (!nodes.containsKey(from) && !nodes.containsKey(from))
      return;
    CallGraphNode nFrom = nodes.get(from);
    CallGraphNode nTo = nodes.get(to);
    if (nFrom != null)
      nFrom.removeCallee(nTo);
    if (nTo != null)
      nTo.removeCaller();
  }

  public void setInvocation(long id, DBInvocation invocation) {
    if (nodes.containsKey(id))
      nodes.get(id).setInvocation(invocation);
    else
      nodes.put(id, new CallGraphNode(invocation, id));
  }

  public CallGraphNode getRootNode() {
    for (CallGraphNode node : nodes.values())
      if (node.getCaller() == null)
        return node;
    return null;
  }

  private void calcPath(long id) {
    nodes.get(id).setPartofPath(true);
    if (nodes.get(id).getCaller() != null)
      calcPath(nodes.get(id).getCaller().getId());
  }

  public void selectNode(long id) {
    nodes.get(id).select();
    for (CallGraphNode n : nodes.values())
      n.setPartofPath(false);
    calcPath(id);
    selectedIDs.add(id);
  }

  public void deselectAll() {
    for (long l : selectedIDs) {
      try {
        nodes.get(l).deselect();
      } catch (NullPointerException exx) {

      } finally {
      }
    }
    selectedIDs.clear();
  }

  public void deselectNode(long id) {
    nodes.get(id).deselect();
    selectedIDs.remove(id);
  }

  public void removeAll(long id) {
    CallGraphNode node;
    if ((node = nodes.get(id)) != null) {
      // while (!(node.getCallees().isEmpty())) {

      // for (CallGraphNode n : node.getCallees()) {
      int sz = node.getCallees().size();
      CallGraphNode[] nodeArray = node.getCallees().toArray(new CallGraphNode[sz]);
      for (CallGraphNode n : nodeArray) {
        removeAll(n.getId());
        if (n.getCaller() != null) {
          removeCallRelation(n.getCaller().getId(), n.getId());
        }
      }
      if (node.getCaller() != null)
        removeCallRelation(node.getCaller().getId(), id);
      nodes.remove(id);
    }
  }

  public void prunePathTo(long id) {
    CallGraphNode node;
   //System.out.println("PRUNE " + id);
    if ((node = nodes.get(id)) != null) {
      if (node.getCaller() == null)
        return;
      // while (!node.getCaller().getCallees().isEmpty()) {
      int sz = node.getCaller().getCallees().size();
      CallGraphNode[] nodeArray = node.getCaller().getCallees().toArray(new CallGraphNode[sz]);
      for (CallGraphNode n : nodeArray) {
   //     System.out.println("HASCALLEES");
        // CallGraphNode n = node.getCaller().getCallees().first();
     //   System.out.println("GOT FIRST " + n);
        if (n != node) {
       //   System.out.println("REMALL " + n);
          removeAll(n.getId());
         // System.out.println("REMRELATION " + node.getCaller().getId());
          removeCallRelation(node.getCaller().getId(), n.getId());
        }
      }
      pruneTo(id);
      node.removeCaller();
    }
  }

  private void pruneTo(long id) {
   // System.out.println("PRUNE TO " + id);
    CallGraphNode node;
    HashSet<Long> tmpIds = new HashSet<>();
    if ((node = nodes.get(id)) != null) {
      if (node.getCaller() != null) {
        tmpIds.add(node.getCaller().getId());
        pruneTo(node.getCaller().getId());
      }

      for (long l : tmpIds) {
        try {
          removeCallRelation(id, l);
        } catch (NullPointerException e) {
        } finally {
          nodes.remove(l);
        }
      }
    }
  }
}
