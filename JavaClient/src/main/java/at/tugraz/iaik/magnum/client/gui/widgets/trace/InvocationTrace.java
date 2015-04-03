/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prﾃｼnster
 * Copyright 2013, 2014 Bernd Prﾃｼnster
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
package at.tugraz.iaik.magnum.client.gui.widgets.trace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.abego.treelayout.Configuration.AlignmentInLevel;
import org.abego.treelayout.Configuration.Location;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import at.tugraz.iaik.magnum.client.cg.CallGraph;
import at.tugraz.iaik.magnum.client.cg.CallGraphNode;
import at.tugraz.iaik.magnum.client.db.IDBUtil;
import at.tugraz.iaik.magnum.client.gui.EvesDropper.DecompileWrapper;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;

public class InvocationTrace extends JPanel {

  private CallGraph                               callGraph;
  private IDBUtil                                 dbUtil;
  private DecompileWrapper                        decompileWrapper;
  private TreeLayout<CallGraphNode>               treeLayout;
  private DefaultTreeForTreeLayout<CallGraphNode> tree;
  private boolean                                 ready;
  private JPopupMenu                              popup;

  public InvocationTrace(CallGraph callgraph, IDBUtil dbUtil, DecompileWrapper decompileWrapper) {
    this.callGraph = callgraph;
    this.dbUtil = dbUtil;
    this.decompileWrapper = decompileWrapper;
    treeLayout = null;
    init();
  }

  abstract class NodeActionListener implements ActionListener {
    protected CallGraphNode node;

    public void setNode(CallGraphNode node) {
      this.node = node;
    }
  }

  class PruneActionListener extends NodeActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      ready = false;
      callGraph.prunePathTo(node.getId());
      draw();
    }
  }

  class CalcListener extends NodeActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      ready = false;
      dbUtil.createInvocationTrace(null, callGraph, node.getId(), true);
      draw();
    }
  }

  class RemoveListener extends NodeActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      ready = false;
      System.out.println("removing");
      callGraph.removeAll(node.getId());
      System.out.println("removed");
      draw();
    }
  }

  class SrcListener extends NodeActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {

      decompileWrapper.decompile(node.getInvocations().first().getUniqueMethodName(), true);
    }
  }
 

  private JPopupMenu createPopup(CallGraphNode node, int x, int y) {
    final JPopupMenu popup = new JPopupMenu();
    final JMenuItem nodeLabel = new JMenuItem();
    nodeLabel.setEnabled(false);
    nodeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    final JTextArea jtaDetail = new JTextArea();
    jtaDetail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    jtaDetail.setEditable(false);

    final JScrollPane detailPane = new JScrollPane(jtaDetail);
    final Dimension areaSize = new Dimension(700, 350);
    final JPanel areaPanel = new JPanel(new BorderLayout());
    areaPanel.add(detailPane, BorderLayout.CENTER);
    areaPanel.setMaximumSize(areaSize);
    nodeLabel.setMaximumSize(new Dimension(100, 30));

    final JMenuItem jmiPruneAll = new JMenuItem("Prune Path Leading Here");
    final JMenuItem jmiCalcHere = new JMenuItem("Calculate Path Leading Here");
    final JMenuItem jmiRemove = new JMenuItem("Remove (Including Children)");
    final JMenuItem jmiSrc = new JMenuItem("Show Source");

    final NodeActionListener pruneListener = new PruneActionListener();
    final CalcListener calcListener = new CalcListener();
    final RemoveListener removeListener = new RemoveListener();
    final SrcListener srcListener = new SrcListener();

    jmiPruneAll.addActionListener(pruneListener);
    jmiCalcHere.addActionListener(calcListener);
    jmiRemove.addActionListener(removeListener);
    jmiSrc.addActionListener(srcListener);
    popup.setMaximumSize(areaSize);

    nodeLabel.setText(node.toString());
    popup.add(nodeLabel);
    popup.add(new JSeparator(JSeparator.HORIZONTAL));
    jtaDetail.setText(node.getInvocations().first().prettyPrint());
    // popup.add(areaItem);
    
    popup.add(areaPanel);
    pruneListener.setNode(node);
    calcListener.setNode(node);
    srcListener.setNode(node);
    removeListener.setNode(node);
    popup.add(new JSeparator(JSeparator.HORIZONTAL));
    popup.add(jmiSrc);
    popup.add(jmiCalcHere);
    popup.add(jmiRemove);
    popup.add(jmiPruneAll);
    popup.show(node.getComponent(), x, y);
    // popup.setMaximumSize(areaSize);
    // popup.setSize(areaSize);
    popup.pack();
    popup.setMaximumSize(areaSize);
    popup.revalidate();
    return popup;
  }

  private void createTree(final CallGraphNode parent) {
    if (parent.getInvocations().size() < 2) {
      if (parent.getComponent().getMouseListeners().length == 0) {
        parent.getComponent().addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            switch (e.getButton()) {
              case MouseEvent.BUTTON1:
                ready = false;
                System.out.println("Delesect");
                callGraph.deselectAll();
                System.out.println("CreateInvocation");
                dbUtil.createInvocationTrace(null, callGraph, parent.getId(), false);
                System.out.println("SELECT");
                callGraph.selectNode(parent.getId());
                System.out.println("DRAW");
                draw();
                System.out.println("DRAWN");
                break;
              case MouseEvent.BUTTON3:

                if (popup != null)
                  popup.setVisible(false);
                popup = createPopup(parent, e.getX(), e.getY());
                break;

            }
          }
        });
      }
    } else {
      parent.getComponent();
      final JComboBox<DBInvocation> cBox = parent.getComboBox();
      if (cBox.getActionListeners().length == 0) // dirty!!!
        cBox.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            ready = false;
            DBInvocation invocation = ((DBInvocation) cBox.getSelectedItem());
            cBox.removeItem(invocation);
            CallGraphNode newNode = new CallGraphNode(invocation, invocation.getId());
            newNode.setIndividual(true);
            parent.removeInvocation(invocation);
            parent.getCaller().addCallee(newNode);
            newNode.setCaller(parent.getCaller());
            System.out.println("newNode: " + newNode.getId());
            System.out.println("parent: " + parent.getId());
            callGraph.update(newNode.getId(), newNode);
            callGraph.update(parent.getId(), parent);
            draw();
          }
        });
    }
    for (CallGraphNode child : parent.getCallees()) {
      tree.addChild(parent, child);
      createTree(child);
    }
  }

  public void draw() {
    ready = false;
    System.gc();
    //
    removeAll();
    tree = new DefaultTreeForTreeLayout<CallGraphNode>(callGraph.getRootNode());
    createTree(callGraph.getRootNode());
    //

    treeLayout = new TreeLayout<CallGraphNode>(tree, new CgNodeExtentProvider(),
        new DefaultConfiguration<CallGraphNode>(50, 20, Location.Left, AlignmentInLevel.Center));

    for (CallGraphNode node : callGraph.getNodes().values())
      paintNode(node);
    ready = true;
    setSize(new Dimension((int) (treeLayout.getBounds().getWidth()), (int) (treeLayout.getBounds().getHeight())));
    setPreferredSize(getSize());
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        repaint();

      }
    });
  }

  private void init() {
    setLayout(null);
    setFont(new Font("sans-serif", Font.PLAIN, 10));

  }

  private void paintNode(CallGraphNode node) {
    Rectangle2D.Double box = treeLayout.getNodeBounds().get(node);
    if (box != null) {

      int x = (int) box.x;
      int y = (int) (box.y);

      node.getComponent().setBounds(x, y, (int) box.width, (int) box.height);
      add(node.getComponent());
      node.getComponent().setVisible(true);
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (ready)
      paintEdges(g, tree.getRoot());
  }

  private void paintEdges(Graphics g, CallGraphNode parent) {
    if (!ready)
      return;
    if (!parent.getCallees().isEmpty()) {
      Rectangle2D.Double b1 = treeLayout.getNodeBounds().get(parent);
      double x1 = b1.getMaxX();
      double y1 = b1.getCenterY();

      for (CallGraphNode child : parent.getCallees()) {
        Rectangle2D.Double b2 = treeLayout.getNodeBounds().get(child);
        if (parent.isPartofPath() && child.isPartofPath())
          g.setColor(Color.RED);
        else
          g.setColor(Color.BLACK);
        g.drawLine((int) x1, (int) y1, (int) b2.getX(), (int) b2.getCenterY());
        paintEdges(g, child);
      }
    }

  }
}
