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
package at.tugraz.iaik.magnum.hook.cmd;

import org.json.JSONException;

import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.MethodHookConfig;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookClassCommand;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookMethodCommand;
import at.tugraz.iaik.magnum.data.cmd.HookUnhookPackageCommand;
import at.tugraz.iaik.magnum.data.cmd.BWListCommand;
import at.tugraz.iaik.magnum.hook.data.Registry;

public abstract class Commander {

  public static void handleCommand(Command cmd) {
    if (cmd instanceof HookUnhookPackageCommand) {
      HookUnhookPackageCommand command = (HookUnhookPackageCommand) cmd;
      if (command.getPackageName().equals(Registry.getPackageName()) && !(command.isHook())) {
        ////Log.d(Constants.TAG, "HOOK Rxd UnhookCommand");
        Registry.getHook().unhookAll();
        Registry.getBridge().disconnect();
        
      }

    } else if (cmd instanceof BWListCommand) {
      ////Log.d(Constants.TAG, "HOOK Rxd BWListCommand");
      BWListCommand command = (BWListCommand) cmd;
      Registry.pureWhiteList = command.isPureWhiteList();
      Registry.globalPackages = command.getPackages();
      Registry.globalPackageWildcards = command.getPackagesWildCards();
      Registry.globalClasses = command.getClasses();
      Registry.globalClassesWildcards = command.getClassesWildcards();
      Registry.globalMethods = command.getMethods();
      Registry.globalMethodWildcards = command.getMethodWildcards();
      Registry.handshake.incrementAndGet();
    } else if (cmd instanceof HookUnhookClassCommand) {
      HookUnhookClassCommand command = (HookUnhookClassCommand) cmd;
      ////Log.d(Constants.TAG, "CLASSUNHOOK");
      ////Log.d(Constants.TAG, command.toString());
      if (command.getPkg().equals(Registry.getPackageName())) {
        for (String className : command.getUnhookedClasses().keySet())
          if (command.getUnhookedClasses().get(className)) {
            Registry.addUnhookedClass(className);
            ////Log.d(Constants.TAG, "Unhooking " + className);
          } else
            Registry.removeUnhookedClass(className);
        // Registry.ready = true;
        Registry.handshake.incrementAndGet();
      }
    } else if (cmd instanceof HookUnhookMethodCommand) {
      HookUnhookMethodCommand command = (HookUnhookMethodCommand) cmd;
      if (command.getPkg().equals(Registry.getPackageName())) {
        ////Log.d(Constants.TAG, "HOOKUNHOOKCONFIG");
        for (MethodHookConfig cfg : command.getMethodHooks()) {
       //   try {
            ////Log.d(Constants.TAG, "CONF: " + cfg.toJSonString());
        //  } catch (JSONException e) {
            // TODO Auto-generated catch block
      //      e.printStackTrace();
      //    }
          Registry.putMethodHookConfig(cfg);
        }
        Registry.ready = true;
        Registry.handshake.incrementAndGet();
      }
    }
  }
}
