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
package at.tugraz.iaik.magnum.hook.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.MethodHookConfig;
import at.tugraz.iaik.magnum.hook.MagnumHook;
import at.tugraz.iaik.magnum.hook.ipc.HookSideBridge;
import at.tugraz.iaik.magnum.util.JavaNameHelper;
import de.robv.android.xposed.XC_MethodHook.Unhook;

public abstract class Registry {

  private static HookSideBridge                    bridge;

  private static String                            packageName;

  private static MagnumHook                        hook;

  private static HashMap<String, MethodHook>       methodHooks          = new HashMap<String, MethodHook>();

  private static HashMap<String, MethodHookConfig> methodHookConfig     = new HashMap<String, MethodHookConfig>();

  private static HashMap<String, Integer>          numMethodInvocations = new HashMap<String, Integer>();

  private static HashSet<String>                   unhookedClasses      = new HashSet<String>();

  public static boolean                            triggered            = true;                                   // sane
                                                                                                                   // default

  public static boolean                            ready                = false;

  public static boolean                            pureWhiteList        = false;

  public static AtomicInteger                      handshake            = new AtomicInteger(0);

  public static Set<String>                        globalPackages;
  public static Set<String>                        globalPackageWildcards;
  public static Set<String>                        globalClasses;
  public static Set<String>                        globalClassesWildcards;
  public static Set<String>                        globalMethods;
  public static Set<String>                        globalMethodWildcards;

  public static void addMethodHook(Member method, Unhook methodHook) {
    String className = method.getDeclaringClass().getName();
    methodHooks.put(className + "." + method.getName(), new MethodHook(method, methodHook));
  }

  public static MethodHook getMethodHook(String fqnm) {
    return methodHooks.get(fqnm);
  }

  public static void removeMethodHook(String fqmn) {
    methodHooks.remove(fqmn);
  }

  public static HookSideBridge getBridge() {
    return bridge;
  }

  public static void setBridge(HookSideBridge bridge) {
    Registry.bridge = bridge;
  }

  public static String getPackageName() {
    return packageName;
  }

  public static void setPackageName(String packageName) {
    Registry.packageName = packageName;
  }

  public static boolean containsMehtodHook(String fqmn) {
    return methodHooks.containsKey(fqmn);
  }

  public static MagnumHook getHook() {
    return hook;
  }

  public static void setHook(MagnumHook hook) {
    Registry.hook = hook;
  }

  public static Collection<MethodHook> getHooks() {
    return methodHooks.values();
  }

  public static void clearHooks() {
    methodHooks.clear();
  }

  public static void addUnhookedClass(String className) {
    unhookedClasses.add(className);
    triggered = false;
  }

  public static void removeUnhookedClass(String className) {
    unhookedClasses.remove(className);
    if ((unhookedClasses.isEmpty()) && (methodHookConfig.isEmpty()))
      triggered = true;
  }

  public static boolean isClassUnhooked(String className) {
    return unhookedClasses.contains(className);
  }

  public static void clearUnhookedClasses() {
    unhookedClasses.clear();
    if (methodHookConfig.isEmpty())
      triggered = true;
  }

  public static MethodHookConfig getMethodHookConfig(String methodName) {
    return methodHookConfig.get(methodName);
  }

  public static void putMethodHookConfig(MethodHookConfig cfg) {
    synchronized (numMethodInvocations) {
      if (cfg.getType() == MethodHookConfig.FULL) {
        methodHookConfig.remove(cfg.getMethodName());
        if ((unhookedClasses.isEmpty()) && (methodHookConfig.isEmpty()))
          triggered = true;
        ////Log.d(Constants.TAG, "cfg received: " + triggered);
        return;
      } else if (cfg.getType() != MethodHookConfig.TRIGGER) {
        numMethodInvocations.put(cfg.getMethodName(), 0);
        triggered = false;
        ////Log.d(Constants.TAG, "cfg received: " + triggered);
      } else {
        if (!ready)
          triggered = false;
      }
      methodHookConfig.put(cfg.getMethodName(), cfg);
    }
  }

  public static int addInvocation(String methodName) {
    synchronized (numMethodInvocations) {
      ////Log.d(Constants.TAG, "ADDING " + methodName);
      if (!numMethodInvocations.containsKey(methodName))
        numMethodInvocations.put(methodName, 0);
      int numIvocations = numMethodInvocations.get(methodName);
      ++numIvocations;
      numMethodInvocations.put(methodName, numIvocations);
      return numIvocations;
    }
  }

  public static void clearMethodHookConfig(String methodName) {
    methodHookConfig.remove(methodName);
    if ((unhookedClasses.isEmpty()) && (methodHookConfig.isEmpty()))
      triggered = true;
  }

  public static boolean checkHookPackage(String pkg) {
    ////Log.d(Constants.TAG, "Checking " + (pureWhiteList ? "witelist" :
    // "blacklist") + " PKG Hook for: " + pkg);
    if (globalPackages.contains(pkg)) {
      ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
      return pureWhiteList;
    }
    for (String str : globalPackageWildcards) {
      if (pkg.startsWith(str)) {
        ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
        return pureWhiteList;
      }
    }
    ////Log.d(Constants.TAG, "Result: " + !pureWhiteList);
    return !pureWhiteList;
  }

  public static boolean checkHookClass(Class<?> clazz) {
    boolean checkPkg = checkHookPackage(clazz.getPackage().getName());

    String className = clazz.getName();
    ////Log.d(Constants.TAG, "Checking " + (pureWhiteList ? "witelist" :
    // "blacklist") + " Class Hook for: " + className);
    if (checkPkg == pureWhiteList) {
      ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
      return pureWhiteList;
    }

    if (globalClasses.contains(className)) {
      ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
      return pureWhiteList;
    }
    for (String cName : globalClassesWildcards) {
      if (className.startsWith(cName)) {
        ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
        return pureWhiteList;
      }
    }
    ////Log.d(Constants.TAG, "Result: " + !pureWhiteList);
    return !pureWhiteList;
  }

  public static boolean checkHookMethod(Member method) {
    boolean checkClass = checkHookClass(method.getDeclaringClass());
    String mName = (method instanceof Constructor<?>) ? "<init>" : method.getName();
    mName = JavaNameHelper.getUniqueMethodName(
        method.getDeclaringClass().getName(),
        method.getName(),
        (method instanceof Constructor<?>) ? (((Constructor<?>) method).getParameterTypes()) : ((Method) method)
            .getParameterTypes());
    List<String> wildcard = new ArrayList<String>(1);
    wildcard.add("*");
    String genericMname = JavaNameHelper.getUniqueMethodName(method.getDeclaringClass().getName(), method.getName(),
        wildcard);
    ////Log.d(Constants.TAG, "Checking " + (pureWhiteList ? "witelist" :
    // "blacklist") + " Method Hook for: " + mName);
    if (checkClass == pureWhiteList) {
      ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
      return pureWhiteList;
    }
    if (globalMethods.contains(mName) || globalMethods.contains(genericMname)) {
      ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
      return pureWhiteList;
    }
    for (String m : globalMethodWildcards)
      if (mName.startsWith(m)) {
        ////Log.d(Constants.TAG, "Result: " + pureWhiteList);
        return pureWhiteList;
      }

    ////Log.d(Constants.TAG, "Result: " + !pureWhiteList);
    return !pureWhiteList;
  }
}
