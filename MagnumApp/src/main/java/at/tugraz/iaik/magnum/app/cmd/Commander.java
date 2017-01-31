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
package at.tugraz.iaik.magnum.app.cmd;

import android.util.Log;
import at.tugraz.iaik.magnum.app.ipc.ServiceSideBridge;
import at.tugraz.iaik.magnum.app.net.PcComm;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.Preferences;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookClassCommand;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookMethodCommand;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookPackageCommand;
import at.tugraz.iaik.magnum.data.cmd.BWListCommand;
import at.tugraz.iaik.magnum.data.cmd.RequestPackageConfigCommand;
import at.tugraz.iaik.magnum.data.transport.TransportObjectBuilder;

public abstract class Commander {

  private static Preferences prefs;
  private static boolean     inited = false;

  public static void init(Preferences prefs) {
    Commander.prefs = prefs;
    inited = true;
  }

  public static void handleCommand(Command cmd) {
    if (!inited)
      return;

    if (cmd instanceof HookUnhookPackageCommand) {
      HookUnhookPackageCommand command = (HookUnhookPackageCommand) cmd;

      String pkgName = command.getPackageName();
      boolean toBeHooked = command.isHook();

      prefs.setPackageHookState(pkgName, toBeHooked);

      ServiceSideBridge bridge = ServiceSideBridge.getInstance();
      bridge.execute(cmd);

      if (toBeHooked) {
        bridge.connect(pkgName);
        ////Log.d("MAGNUM", "Commander: connected bridge");
      } else {
        bridge.disconnect(pkgName);
        ////Log.d("MAGNUM", "Commander: killed bridge");
      }

    } else if (cmd instanceof RequestPackageConfigCommand) {
      try {
        ////Log.d(Constants.TAG, "Send Config");
        PcComm.getInstance().write(TransportObjectBuilder.buildForPackageConfig(prefs.getPackageConfig()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (cmd instanceof HookUnhookMethodCommand) {
      try {
        ////Log.d(Constants.TAG, "HUMC");
        HookUnhookMethodCommand command = (HookUnhookMethodCommand) cmd;
        prefs.setMethodHookState(command.getPkg(), command.getMethodHooks());
        ServiceSideBridge bridge = ServiceSideBridge.getInstance();
        bridge.execute(cmd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (cmd instanceof HookUnhookClassCommand){
      HookUnhookClassCommand command = (HookUnhookClassCommand) cmd;
      prefs.setClassHookState(command.getPkg(), command.getUnhookedClasses());
      ServiceSideBridge bridge = ServiceSideBridge.getInstance();
      bridge.execute(cmd);
     
    }else if(cmd instanceof BWListCommand){
      BWListCommand command = (BWListCommand) cmd;
      prefs.setGlobalHooks(command);
    }

  }

}
