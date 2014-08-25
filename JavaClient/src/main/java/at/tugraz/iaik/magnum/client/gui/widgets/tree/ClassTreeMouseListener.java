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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import at.tugraz.iaik.magnum.client.conf.RuntimeConfig;
import at.tugraz.iaik.magnum.client.net.Communication;
import at.tugraz.iaik.magnum.client.util.Injector;
import at.tugraz.iaik.magnum.conf.MethodHookConfig;
import at.tugraz.iaik.magnum.conf.PackageConfig;
import at.tugraz.iaik.magnum.data.cmd.CommandBuilder;
import at.tugraz.iaik.magnum.model.MethodModel;

public class ClassTreeMouseListener implements MouseListener {
  private JTree tree;

  public ClassTreeMouseListener(JTree tree) {
    this.tree = tree;
  }

  @Override
  public void mouseClicked(MouseEvent e) {

    if (!SwingUtilities.isRightMouseButton(e))
      return;

    Object elem = tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
    if (elem instanceof MethodTreeNode) {
      final MethodModel methodModel = ((MethodTreeNode) elem).getMethodModel();
      final String methodName = methodModel.getUniqueMethodName();

      System.out.println("Selected method: " + methodName);

      JPopupMenu popupMenu = new JPopupMenu();

      RuntimeConfig conf = (RuntimeConfig) Injector.get(RuntimeConfig.class);
      final PackageConfig pkgConf = conf.getPackageConfig(conf.getCurrentPackage());

      if (pkgConf != null) {

        MethodHookConfig mhc = pkgConf.getMethodHooks().get(methodName);
        byte type = MethodHookConfig.FULL;
        if (mhc != null) {
          type = mhc.getType();
        }
        ButtonGroup bg = new ButtonGroup();
        JRadioButton jrbNone = new JRadioButton("None");
        jrbNone.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) {
              setMethodHookState(pkgConf, methodName, MethodHookConfig.NONE, 0);
            }
          }
        });
        JRadioButton jrbFull = new JRadioButton("Full");
        jrbFull.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) {
              setMethodHookState(pkgConf, methodName, MethodHookConfig.FULL, 0);
            }
          }
        });
        JRadioButton jrbTrigger = new JRadioButton("Trigger");
        jrbTrigger.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) {
              String numInvocations = JOptionPane.showInputDialog(tree, "Number Of Invocations", methodModel
                  .getInvocations().size());
              if (numInvocations != null) {
                try {
                  int incovations = Integer.parseInt(numInvocations);
                  setMethodHookState(pkgConf, methodName, MethodHookConfig.TRIGGER, incovations);
                } catch (NumberFormatException ex) {
                  JOptionPane.showMessageDialog(tree, "NaN!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
              }
            }
          }
        });

        bg.add(jrbNone);
        bg.add(jrbFull);
        bg.add(jrbTrigger);
        switch (type) {
          case MethodHookConfig.FULL:
            jrbFull.setSelected(true);
            break;
          case MethodHookConfig.NONE:
            jrbNone.setSelected(true);
            break;
          case MethodHookConfig.TRIGGER:
            jrbTrigger.setText(jrbTrigger.getText() + ": " + mhc.getNumInvocations() + " Invocations");
            jrbTrigger.setSelected(true);
            break;
        }
        popupMenu.add(jrbNone);
        popupMenu.add(jrbFull);
        popupMenu.add(jrbTrigger);
      }

      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    } else if (elem instanceof ClassTreeNode) {
      final ClassTreeNode node = (ClassTreeNode) elem;
      JPopupMenu popupMenu = new JPopupMenu();

      RuntimeConfig conf = (RuntimeConfig) Injector.get(RuntimeConfig.class);
      final PackageConfig pkgConf = conf.getPackageConfig(conf.getCurrentPackage());

      if (pkgConf != null) {
        boolean hooked = true;
        if (pkgConf.getUnhookedClasses().contains(node.getClassModel().getClassName())) {
          hooked = false;
        }
        JMenuItem jmiHook = new JMenuItem(hooked ? "Unhook" : "Hook");
        jmiHook.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            Set<String> unhook = new HashSet<String>();
            boolean doHook = !pkgConf.getUnhookedClasses().contains(node.getClassModel().getClassName());
            if (doHook) {
              pkgConf.getUnhookedClasses().remove(node.getClassModel().getClassName());
            } else {
              pkgConf.getUnhookedClasses().add(node.getClassModel().getClassName());
            }
            unhook.add(node.getClassModel().getClassName());

            Communication comm = (Communication) Injector.get(Communication.class);
            try {
              comm.write(CommandBuilder.buildForHookUnhookClassCommand(pkgConf.getPkg(), unhook, doHook));
            } catch (ConnectException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          }
        });

        popupMenu.add(jmiHook);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }

    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  private void setMethodHookState(PackageConfig pkgConf, String methodName, byte state, int invocations) {
    Communication comm = (Communication) Injector.get(Communication.class);
    if (state != MethodHookConfig.TRIGGER)
      pkgConf.getMethodHooks().put(methodName, new MethodHookConfig(methodName, state));
    else
      pkgConf.getMethodHooks().put(methodName, new MethodHookConfig(methodName, invocations));
    try {
      comm.write(CommandBuilder.buildForHookUnhookMethodCommand(pkgConf));
    } catch (ConnectException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
