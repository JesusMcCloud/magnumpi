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
package at.tugraz.iaik.magnum.conf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import at.tugraz.iaik.magnum.data.cmd.BWListCommand;

public class Preferences {

  private static final String              HOOKED_PACKAGES = "hokedPackages";
  private SharedPreferences                prefs;
  private final Map<String, PackageConfig> packageConfig;
  private Context                          ctx;
  private BWListCommand                  initHookCommand;

  private static Preferences               instance        = null;

  public static Preferences getInstance(Context ctx) {
    if (instance == null)
      instance = new Preferences(ctx);
    return instance;
  }

  private Preferences(Context ctx) {
    this.ctx = ctx;
    try {
      prefs = ctx.createPackageContext(Constants.MAGNUM_CONTEXT, Context.MODE_PRIVATE).getSharedPreferences(
          HOOKED_PACKAGES, Context.MODE_PRIVATE);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
      prefs = null;
    }
    Log.d("PREF", prefs == null ? "null" : prefs.toString());
    packageConfig = getPackageSettings();
  }

  private List<PackageConfig> listInstalledPackages() {
    List<PackageInfo> packages = ctx.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
    List<PackageConfig> pkgNames = new LinkedList<PackageConfig>();
    for (PackageInfo info : packages) {
      pkgNames.add(new PackageConfig(info.packageName, (String) ctx.getPackageManager().getApplicationLabel(
          info.applicationInfo)));
      Log.d("PKGNAME", info.packageName);
    }
    return pkgNames;
  }

  private Map<String, PackageConfig> getPackageSettings() {
    List<PackageConfig> installedPkgs = listInstalledPackages();
    Set<String> enabledHooks = prefs.getStringSet(HOOKED_PACKAGES, new HashSet<String>());
    final Map<String, PackageConfig> config = new HashMap<String, PackageConfig>();
    for (PackageConfig s : installedPkgs) {
      if (enabledHooks.contains(s.getPkg())) {
        s.setHooked(true);
        Set<String> methodHooks = prefs.getStringSet(s.getPkg() + "_methods", new HashSet<String>());
        for (String mhook : methodHooks) {
          MethodHookConfig mConf;
          try {
            mConf = MethodHookConfig.fromJsonString(mhook);
            s.addMethodHookConfig(mConf.getMethodName(), mConf);
            Log.d("PKGCONF", "Method: " + mConf.getMethodName() + ": " + mConf.getType());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        Set<String> unhookedClasses = prefs.getStringSet(s.getPkg() + "_unhookedClasses", new HashSet<String>());
        for (String str : unhookedClasses) {
          Log.d("PKGCONF", "Class: " + str);
        }
        s.addUnhookedClasses(unhookedClasses);
      } else
        s.setHooked(false);
      config.put(s.getPkg(), s);
    }
    return config;
  }

  private void save() {
    Set<String> enabledHooks = new HashSet<String>();
    Editor editor = prefs.edit();
    for (String pkg : packageConfig.keySet()) {
      String key_methods = pkg + "_methods";
      String key_classes = pkg + "_unhookedClasses";
      PackageConfig pConfig = packageConfig.get(pkg);
      if (pConfig.isHooked()) {
        enabledHooks.add(pkg);
        Set<String> methods = new HashSet<String>();

        for (MethodHookConfig m : pConfig.getMethodHooks().values())
          try {
            methods.add(m.toJSonString());
          } catch (JSONException e) {
            e.printStackTrace();
          }
        editor.putStringSet(key_methods, methods);
        editor.putStringSet(key_classes, pConfig.getUnhookedClasses());
      }
    }
    editor.putStringSet(HOOKED_PACKAGES, enabledHooks);
    editor.commit();
  }

  public Map<String, PackageConfig> getPackageConfig() {
    return packageConfig;
  }

  public void setPackageHookState(String packageName, boolean hooked) {
    packageConfig.get(packageName).setHooked(hooked);
    save();
  }

  public Set<PackageConfig> getHookedPackages() {
    Set<PackageConfig> pkgs = new HashSet<PackageConfig>();
    for (String p : packageConfig.keySet()) {
      PackageConfig pConf = packageConfig.get(p);
      if (pConf.isHooked())
        pkgs.add(pConf);
    }
    return pkgs;
  }

  public void setMethodHookState(String pkg, Set<MethodHookConfig> methodHooks) {
    for (MethodHookConfig mhc : methodHooks)
      packageConfig.get(pkg).addMethodHookConfig(mhc.getMethodName(), mhc);
    save();
  }

  public void setClassHookState(String pkg, Map<String, Boolean> map) {
    for (String clazz : map.keySet()) {
      Log.d("MAGNUM", "UNHOOKING CLASS: " + clazz + "? " + map.get(clazz));
      if (map.get(clazz))
        packageConfig.get(pkg).addUnhookedClass(clazz);
      else
        packageConfig.get(pkg).removeUnhookedClass(clazz);
    }

    save();
  }

  public void setGlobalHooks(BWListCommand command) {
    initHookCommand = command;
  }

  public BWListCommand getGlobalHooks() {
    return initHookCommand;
  }
}
