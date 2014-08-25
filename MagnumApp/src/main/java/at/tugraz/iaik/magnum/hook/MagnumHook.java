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
package at.tugraz.iaik.magnum.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.MethodHookConfig;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObjectBuilder;
import at.tugraz.iaik.magnum.hook.data.MethodHook;
import at.tugraz.iaik.magnum.hook.data.Registry;
import at.tugraz.iaik.magnum.hook.data.StaticBlackWhiteList;
import at.tugraz.iaik.magnum.hook.ipc.HookSideBridge;
import at.tugraz.iaik.magnum.util.JavaNameHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MagnumHook extends Application implements IXposedHookLoadPackage, Constants {

  private MessageQueue           messageQueue;
  private HookSideBridge         bridge;

  private AtomicBoolean          barrier;
  private Set<String>            patchedClasses;
  private Set<Method>            hookedMethods;
  private Set<Constructor>       hookedCtors;
  private IDGenerator            idGen;
  private StaticBlackWhiteList   bwList;
  private Map<Long, Stack<Long>> stackmap;

  public MagnumHook() {
    Log.d(TAG, "Loaded Magnum Packet Inspector");
    stackmap = new HashMap<Long, Stack<Long>>();
    idGen = IDGenerator.getInstance();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
    bwList = new StaticBlackWhiteList();
    if (bwList.isPackageBlackListed(lpparam.packageName))
      return;

    // hooking makes it SLOOOOOOOOOOOOW!!!!!
    disableStrictMode();
    Registry.setHook(this);
    Registry.setPackageName(lpparam.packageName);
    Registry.ready = false;
    Registry.handshake.set(0);
    ;
    messageQueue = new MessageQueue();
    bridge = new HookSideBridge(messageQueue);
    Registry.setBridge(bridge);

    try {
      bridge.retreiveInitCommand(lpparam.packageName);
      Log.d(TAG, "start handleLoadPackage()");
      Log.d(TAG, "hooking " + lpparam.packageName);
      Log.d(TAG, Registry.pureWhiteList ? "Pure white-list mode engaged" : "Regular mode engaged.");
    } catch (Exception e) {
      Log.w(TAG, lpparam.packageName + ": Magnum service not running");
      return;
    }

    barrier = new AtomicBoolean();
    patchedClasses = new HashSet<String>();
    hookedMethods = new HashSet<Method>();
    hookedCtors = new HashSet<Constructor>();
    idGen.reset();

    bridge.connect();

    Log.d(TAG, "handleLoadPackage for: " + lpparam.packageName);
    String apkPath = lpparam.appInfo.sourceDir;
    Log.d(TAG, "source dir is: " + apkPath);

    postTransportObject(TransportObjectBuilder.buildForApkFile(apkPath, lpparam.packageName));
    Log.d(TAG, "Sent APK");

    while (Registry.handshake.get() < 3) {
      Thread.sleep(100);
    }
    hookToClassloader(lpparam.classLoader, apkPath);
  }

  private void disableStrictMode() {
    // hooking makes it SLOOOOOOOOOOOOW!!!!!
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    StrictMode.setThreadPolicy(policy);
  }

  private void hookToClassloader(final ClassLoader classLoader, final String sourceDir) {

    Class<?> classLoaderClazz = classLoader.getClass();

    try {
      final Method method = classLoaderClazz.getMethod("loadClass", String.class);
      XC_MethodHook classloaderHook = getClassLoaderHook(classLoader, getMethodLevelHook());
      Unhook hook = XposedBridge.hookMethod(method, classloaderHook);
      Registry.addMethodHook(method, hook);
    } catch (NoSuchMethodException e) {
      Log.e(TAG, "Exception when reflecting ClassLoader.loadClass(): " + e.getMessage().toString());
    }
  }

  private XC_MethodHook getClassLoaderHook(final ClassLoader classLoader, final XC_MethodHook methodLevelHook) {
    return new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        // Avoid infinite loop
        if (barrier.get()) {
          return;
        }

        final String loadedClass = (String) param.args[0];

        if (isClassAlreadyPatched(loadedClass))
          return;

        if (Registry.isClassUnhooked(loadedClass)) {
          Log.d(TAG, "Ignoring user blacklsited class: " + loadedClass);
          return;
        }

        if (bwList.isClassBlacklisted(loadedClass)) {
          Log.d(TAG, "Ignoring blacklisted class: " + loadedClass);
          return;
        }

        try {
          barrier.set(true);
          Class<?> classOfInterest = XposedHelpers.findClass(loadedClass, classLoader);
          if (bwList.isSuperClassBlacklisted(classOfInterest)) {
            barrier.set(false);
            Log.d(TAG, "Ignoring superclass-blacklisted class: " + loadedClass);
            return;
          }

          if (!Registry.pureWhiteList) { // otherwise check later
            if (!Registry.checkHookPackage(classOfInterest.getPackage().getName())) {
              barrier.set(false);
              Log.d(TAG, "Ignoring globally package-blacklisted class: " + loadedClass);
              return;
            }
            if (!Registry.checkHookClass(classOfInterest)) {
              barrier.set(false);
              Log.d(TAG, "Ignoring globally blacklisted class: " + loadedClass);
              return;
            }
          }

          final Set<Method> methods = new HashSet<Method>();

          final Method[] publicMethods = classOfInterest.getMethods();
          methods.addAll(Arrays.asList(publicMethods));

          Method[] declaredMethods = classOfInterest.getDeclaredMethods();
          methods.addAll(Arrays.asList(declaredMethods));

          final Set<Constructor> constructors = new HashSet<Constructor>();

          final Constructor[] publicConstructors = classOfInterest.getConstructors();
          constructors.addAll(Arrays.asList(publicConstructors));
          Constructor[] declaredConstrucotrs = classOfInterest.getDeclaredConstructors();
          constructors.addAll(Arrays.asList(declaredConstrucotrs));

          // Log.d(TAG, "Patching class " + loadedClass + "; # of methods =" +
          // methods.size());

          postTransportObject(TransportObjectBuilder.buildForLoadClass(classOfInterest));

          for (Method method : methods) {
            try {
              if (bwList.isSuperClass(classOfInterest, Thread.class)) {
                if (bwList.isThreadMethodButStart(method)) {
                  continue;
                } else if (method.getName().equals("start")) {
                  XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                      long tid = ((Thread) param.thisObject).getId();

                      if (!stackmap.containsKey(tid))
                        stackmap.put(tid, new Stack<Long>());
                      try {
                        stackmap.get(tid).push(stackmap.get(Thread.currentThread().getId()).peek());

                      } catch (EmptyStackException e) {
                        Log.e(TAG, "THREAD STACK EMPTY");
                      }

                    }
                  });
                  continue;
                }
              }

              if (Modifier.isAbstract(method.getModifiers())) {
                Log.d(TAG, "Skipping abstract method: " + loadedClass + "." + method.getName());
                continue;
              }

              if (bwList.isBlacklisted(method)) {
                Log.d(TAG, "Skipping blacklisted method: " + method.getClass().getName() + "." + method.getName());
                continue;
              }

              if (!hookedMethods.contains(method)) {

                if (!Registry.checkHookMethod(method)) {
                  Log.d(TAG, "Ignoring globally blacklisted method: " + loadedClass + "." + method.getName());
                  continue;
                }

                hookedMethods.add(method);
                TransportObject msg = TransportObjectBuilder.buildForMethodHook(method);

                postTransportObject(msg);
                Unhook hook = XposedBridge.hookMethod(method, methodLevelHook);
                Registry.addMethodHook(method, hook);

              }
            } catch (Exception e) {
              for (StackTraceElement el : e.getStackTrace()) {
                Log.e(TAG, el.toString());
              }
            }
          }

          for (Constructor ctor : constructors) {
            try {
              if (Modifier.isAbstract(ctor.getModifiers())) {
                continue;
              }

              if (!hookedCtors.contains(ctor)) {
                if (!Registry.checkHookMethod(ctor)) {
                  Log.d(TAG, "Ignoring globally blacklisted constructor of class: " + loadedClass);
                  continue;
                }
                hookedCtors.add(ctor);
                TransportObject msg = TransportObjectBuilder.buildForMethodHook(ctor);

                postTransportObject(msg);
                Unhook hook = XposedBridge.hookMethod(ctor, methodLevelHook);
                Registry.addMethodHook(ctor, hook);

              }
            } catch (Exception e) {
              for (StackTraceElement el : e.getStackTrace()) {
                Log.e(TAG, el.toString());
              }
            }
          }

          postTransportObject(TransportObjectBuilder.buildForDonePatchingClass(loadedClass));
        } catch (ClassNotFoundError e) {
        } finally {
          rememberClass(loadedClass);
          barrier.set(false);
        }
      }

      private boolean isClassAlreadyPatched(String loadedClass) {
        return patchedClasses.contains(loadedClass);
      }

      private void rememberClass(String className) {
        patchedClasses.add(className);
        Log.d(TAG, "Remembering " + className);
      }
    };
  }

  private void printStack(StackTraceElement[] trace) {
    int i = 0;
    for (StackTraceElement el : trace) {
      Log.d("MAGTRACE", i++ + " " + el.getClassName());
    }
  }

  private XC_MethodHook getMethodLevelHook() {
    return new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        /*
         * Apache’s Java implementation tries to come up with a
         * SERIAL_VERSION_UID when none is present. The algorithm applied uses a
         * MessageDigest over a byte array created from properties of the class
         * obtained via reflection. When analysing LastPass we encounter
         * Serializable classes that have no explicit SERIAL_VERSION_UID set. We
         * end up in an infinite loop and crash with a stack overflow since we
         * cannot black-list MessageDigest.
         * http://opensourcejavaphp.net/java/harmony
         * /java/io/ObjectStreamClass.java.html
         */

        if (param.method.getDeclaringClass().getName().equals("java.security.MessageDigest")) {
          StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
          for (StackTraceElement el : stackTraceElements)
            if (el.getClassName().equals("java.io.ObjectStreamClass")) {
              return;
            }
        }

        boolean haveID = false;
        // available
        long prevID = 0;

        long currentThreadID = Thread.currentThread().getId(); // Stackoverflow
        synchronized (stackmap) {
          if (!stackmap.containsKey(currentThreadID))
            stackmap.put(currentThreadID, new Stack<Long>());
          try {
            prevID = stackmap.get(currentThreadID).peek();
            haveID = true;
          } catch (Exception e) {

          }
        }

        // spaghetticode is more efficient than method calls
        if (!Registry.triggered) {
          if (Registry.isClassUnhooked(param.method.getDeclaringClass().getName()))
            return;
          String methodName;

          if (param.method instanceof Method)
            methodName = JavaNameHelper.getUniqueMethodName(param.method.getDeclaringClass().getName(),
                param.method.getName(), ((Method) param.method).getParameterTypes());
          else
            methodName = JavaNameHelper.getUniqueMethodName(param.method.getDeclaringClass().getName(), "<init>",
                ((Constructor) param.method).getParameterTypes());

          MethodHookConfig methodHookConfig = Registry.getMethodHookConfig(methodName);
          if (methodHookConfig == null) {
            long identifier = idGen.getID();
            stackmap.get(currentThreadID).push(identifier);
            stackmap.get(Thread.currentThread().getId()).push(identifier);
            TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, param.args, identifier,
                prevID, haveID);
            param.setObjectExtra("magnumCallIdentifier", identifier);
            postTransportObject(msg);
          } else {
            Log.d(TAG, methodHookConfig.toJSonString());
            if (methodHookConfig.getType() == MethodHookConfig.NONE)
              return; // this should never happen
            else if (methodHookConfig.getType() == MethodHookConfig.TRIGGER) {
              if (Registry.addInvocation(methodName) >= methodHookConfig.getNumInvocations()) {
                Registry.clearMethodHookConfig(methodName);
                long identifier = idGen.getID();
                stackmap.get(currentThreadID).push(identifier);
                TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, param.args, identifier,
                    prevID, haveID);
                param.setObjectExtra("magnumCallIdentifier", identifier);
                postTransportObject(msg);
              }
            }
          }
        } else {
          long identifier = idGen.getID();
          stackmap.get(currentThreadID).push(identifier);
          try {
            TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, param.args, identifier,
                prevID, haveID);
            postTransportObject(msg);

          } catch (StackOverflowError err) {
            Log.e("MAGSTACK",
                err.getMessage() + ",\n" + param.method.getDeclaringClass() + "." + param.method.getName());
            // err.printStackTrace();
          }
          param.setObjectExtra("magnumCallIdentifier", identifier);
        }
      }

      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {

        Long identifier = (Long) param.getObjectExtra("magnumCallIdentifier");
        // Can never be null, unless we skipped something
        if (identifier == null)
          return;

        try {
          synchronized (stackmap) {
            stackmap.get(Thread.currentThread().getId()).pop();
          }
        } catch (Exception e) {
          Log.e("MAGNUM",
              "************************************************ STACKMAP CORRUPTED!!! *********************************************");
          Log.e("MAGNUM", "*** " + param.method.getDeclaringClass().getCanonicalName() + "." + param.method.getName());
        }

        TransportObject msg = TransportObjectBuilder.buildForMethodExit(param.method, param.getResult(), identifier);

        postTransportObject(msg);
      }
    };
  }

  private void postTransportObject(final TransportObject to) {
    messageQueue.put(to);
  }

  public synchronized void unhookMethod(String methodName) {
    if (!Registry.containsMehtodHook(methodName))
      return;

    MethodHook hook = Registry.getMethodHook(methodName);

    hook.callback.unhook();
    Registry.removeMethodHook(methodName);
  }

  public synchronized void unhookAll() {
    Log.d(Constants.TAG, "Unhooking everything!");
    for (MethodHook hook : Registry.getHooks()) {
      hook.callback.unhook();
      Log.d(TAG, "unhooking " + hook.method.getDeclaringClass() + "." + hook.method.getName());
    }

    Registry.clearHooks();
  }

  public void shutdown() {
    unhookAll();
    messageQueue.clear();
    bridge.disconnect();
  }
}
