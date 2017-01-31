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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
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
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.json.JSONException;
import org.json.JSONObject;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class MagnumHook extends Application implements IXposedHookLoadPackage, Constants {

  private MessageQueue           messageQueue;
  private HookSideBridge         bridge;

  private AtomicBoolean          barrier;
  private Set<String>            patchedClasses;
  private Set<java.lang.reflect.Method>            hookedMethods;
  private Set<Constructor>       hookedCtors;
  private IDGenerator            idGen;
  private StaticBlackWhiteList   bwList;
  private Map<Long, Stack<Long>> stackmap;
  private Set<? extends ClassDef> dexClasses;

  public MagnumHook() {
    ////Log.d(TAG, "Loaded Magnum Packet Inspector");
    stackmap = new HashMap<Long, Stack<Long>>();
    idGen = IDGenerator.getInstance();
    dexClasses = new HashSet<ClassDef>();
  }

  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

    bwList = new StaticBlackWhiteList();
    if (bwList.isPackageBlackListed(lpparam.packageName))
      return;

    /**********************************************************/
    XposedBridge.log("Loaded app: " + lpparam.packageName);
    //Log.d(TAG, "Loaded app: " + lpparam.packageName);

    // hooking makes it SLOOOOOOOOOOOOW!!!!!
    disableStrictMode();

    Registry.setHook(this);
    Registry.setPackageName(lpparam.packageName);
    Registry.ready = false;
    Registry.handshake.set(0);

    messageQueue = new MessageQueue();
    bridge = new HookSideBridge(messageQueue);
    Registry.setBridge(bridge);


    try {
      //Log.d(TAG, "before bridge.retreiveInitCommand");
      bridge.retreiveInitCommand(lpparam.packageName);
      //Log.d(TAG, "bridge.retreiveInitCommand finished");
      ////Log.d(TAG, "hooking " + lpparam.packageName);
      ////Log.d(TAG, Registry.pureWhiteList ? "Pure white-list mode engaged" : "Regular mode engaged.");
    } catch (Exception e) {
      Log.w(TAG, lpparam.packageName + ": Magnum service not running");
      return;
    }
    barrier = new AtomicBoolean();
    patchedClasses = new HashSet<String>();
    hookedMethods = new HashSet<java.lang.reflect.Method>();
    hookedCtors = new HashSet<Constructor>();
    idGen.reset();

    bridge.connect();

    //Log.d(TAG, "handleLoadPackage for: " + lpparam.packageName);
    String apkPath = lpparam.appInfo.sourceDir;
    //Log.d(TAG, "source dir is: " + apkPath);

    postTransportObject(TransportObjectBuilder.buildForApkFile(apkPath, lpparam.packageName));
    //Log.d(TAG, "Sent APK");

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

  private void retrieveDexClasses(final String sourceDir) {
    DexFile dexFile = null;
    try {
      dexFile = DexFileFactory.loadDexFile(new File(sourceDir), 16 );
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (dexFile != null) {
      this.dexClasses = dexFile.getClasses();
    }
  }

  /***
   * className is used to return a list of classes that start with this class name in
   * the list of all smali classes
   * e.g. className = com.example.anon.MainActivity
   * The function will return classes like
   *   com.example.anon.MainActivity$1
   *   com.example.anon.MainActivity$2
   *
   * @param className The string with which the class names should start
   * @return A list of found classes
   */
  private List<String> findDexClassesStartingWithClassName(String className)
  {
    List<String> foundClasses = new ArrayList<String>();

    for (ClassDef classDef : this.dexClasses) {
      String foundClazz = classDef.getType().substring(1).replace('/','.').replaceAll(";","");
      if(foundClazz.startsWith(className))
      {
        //Log.d(TAG, "found class in dexClasses: " + foundClazz);
        foundClasses.add(foundClazz);
      }
    }
    return foundClasses;
  }


  private Object[] getParametersInJSONStructure(Object[] parameters) throws JSONException {

  //  ObjectMapper mapper = new ObjectMapper();
    //By default all fields without explicit view definition are included, disable this
  //  mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    Object[] objectsArray = new Object[0];
    if (parameters != null && parameters.length != 0) {
      objectsArray = new Object[parameters.length];
      for (int pos = 0; pos < parameters.length; pos++) {
        if (parameters[pos] != null) {
          JSONObject jsonParameters = new JSONObject();
          objectsArray[pos] = jsonParameters.put(parameters[pos].getClass().getCanonicalName(), parameters[pos].toString());

          /*
           try {
            objectsArray[pos] = jsonParameters.put((pos+1) + ". Arg", mapper.writeValueAsString(parameters[pos]));
          } catch (JsonProcessingException e) {
            //Log.d("JACKSON", e.getMessage());
          }
           */
        }
      }
    }
    return objectsArray;
  }




  private void hookToClassloader(final ClassLoader classLoader, final String sourceDir) {

    //Log.d(TAG, "hookToClassloader");
    Class<?> classLoaderClazz = classLoader.getClass();

    retrieveDexClasses(sourceDir);

    try {
      //de.robv.android.xposed.XposedHelpers.findandhookm
      final java.lang.reflect.Method method = classLoaderClazz.getMethod("loadClass", String.class);
      //Log.d(TAG, "hookToClassloader before classloaderHook");
      //Log.d(TAG, "hooked method: class: " + method.getClass() + " method " + method.getName());
      XC_MethodHook classloaderHook = getClassLoaderHook(classLoader, getMethodLevelHook(), sourceDir);


      Unhook hook = XposedBridge.hookMethod(method, classloaderHook);
      Registry.addMethodHook(method, hook);
    } catch (NoSuchMethodException e) {
      Log.e(TAG, "Exception when reflecting ClassLoader.loadClass(): " + e.getMessage());
    }
  }

  private XC_MethodHook getClassLoaderHook(final ClassLoader classLoader, final XC_MethodHook methodLevelHook, final String sourceDir) {
    return new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        //Log.d(TAG, "In getClassLoaderHook:XC_MethodHook, class: " + param.method.getClass() + "##" + param.method.getName());
        // Avoid infinite loop
        if (barrier.get()) {
          return;
        }

        final String classLoaded = (String) param.args[0];
        //Log.d(TAG, "param 0 = loadedclass: " + classLoaded);
        if (param.args.length > 1) {
          //Log.d(TAG, "params: " + param.args.length + " 2. param: " + param.args[1]);

        }

        List<String> foundClasses = findDexClassesStartingWithClassName(classLoaded);
        if (foundClasses.isEmpty()) {
          foundClasses.add(classLoaded);
        }

        for (String loadedClass : foundClasses) {
          //Log.d(TAG, "in for, size: " + foundClasses.size() + " loadedClass: " + loadedClass);

          if (isClassAlreadyPatched(loadedClass))
            return;

          if (Registry.isClassUnhooked(loadedClass)) {
            ////Log.d(TAG, "Ignoring user blacklsited class: " + loadedClass);
            return;
          }

          if (bwList.isClassBlacklisted(loadedClass)) {
            ////Log.d(TAG, "Ignoring blacklisted class: " + loadedClass);
            return;
          }

          try {
            barrier.set(true);
            Class<?> classOfInterest = findClass(loadedClass, classLoader);
            if (bwList.isSuperClassBlacklisted(classOfInterest)) {
              barrier.set(false);
              //Log.d(TAG, "Ignoring superclass-blacklisted class: " + loadedClass);
              return;
            }

            if (!Registry.pureWhiteList) { // otherwise check later
              if (!Registry.checkHookPackage(classOfInterest.getPackage().getName())) {
                barrier.set(false);
                //Log.d(TAG, "Ignoring globally package-blacklisted class: " + loadedClass);
                return;
              }
              if (!Registry.checkHookClass(classOfInterest)) {
                barrier.set(false);
                ////Log.d(TAG, "Ignoring globally blacklisted class: " + loadedClass);
                return;
              }
            }

            final Set<java.lang.reflect.Method> methods = new HashSet<java.lang.reflect.Method>();

            final java.lang.reflect.Method[] publicMethods = classOfInterest.getMethods();
            methods.addAll(Arrays.asList(publicMethods));

            java.lang.reflect.Method[] declaredMethods = classOfInterest.getDeclaredMethods();
            methods.addAll(Arrays.asList(declaredMethods));

            final Set<Constructor> constructors = new HashSet<Constructor>();

            final Constructor[] publicConstructors = classOfInterest.getConstructors();
            constructors.addAll(Arrays.asList(publicConstructors));
            Constructor[] declaredConstrucotrs = classOfInterest.getDeclaredConstructors();
            constructors.addAll(Arrays.asList(declaredConstrucotrs));

            //Log.d(TAG, "Patching class " + loadedClass + "; # of methods =" + methods.size());

            postTransportObject(TransportObjectBuilder.buildForLoadClass(classOfInterest));

            for (java.lang.reflect.Method method : methods) {
              //Log.d(TAG, "for methods -  class: " + method.getDeclaringClass() + " method: " + method.getName());

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
                  ////Log.d(TAG, "Skipping abstract method: " + loadedClass + "." + method.getName());
                  continue;
                }

                if (bwList.isBlacklisted(method)) {
                  ////Log.d(TAG, "Skipping blacklisted method: " + method.getClass().getName() + "." + method.getName());
                  continue;
                }

                if (!hookedMethods.contains(method)) {

                  if (!Registry.checkHookMethod(method)) {
                    ////Log.d(TAG, "Ignoring globally blacklisted method: " + loadedClass + "." + method.getName());
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
                    ////Log.d(TAG, "Ignoring globally blacklisted constructor of class: " + loadedClass);
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
      }

      private boolean isClassAlreadyPatched(String loadedClass) {
        return patchedClasses.contains(loadedClass);
      }

      private void rememberClass(String className) {
        patchedClasses.add(className);
        ////Log.d(TAG, "Remembering " + className);
      }
    };
  }

  private void printStack(StackTraceElement[] trace) {
    int i = 0;
    for (StackTraceElement el : trace) {
      //Log.d("MAGTRACE", i++ + " " + el.getClassName());
    }
  }


  private XC_MethodHook getMethodLevelHook() {
    return new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

        //Log.d(TAG, "In XC_MethodHook beforeHookedMethod: ");

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

        //Log.d(TAG, "beforeHookedMethod: method: " +  param.method.getName());

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
          } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
          }
          catch(NullPointerException e)
          {
            Log.e(TAG, e.getMessage());
          }
          catch(EmptyStackException e)
          {
            Log.e(TAG, "Stack is empty!");
          }
        }

        // spaghetticode is more efficient than method calls
        if (!Registry.triggered) {
          if (Registry.isClassUnhooked(param.method.getDeclaringClass().getName()))
          {
            //Log.d(TAG, "beforeHookedMethod: class is unhooked - return - method: " +  param.method.getName());
            return;
          }

          String methodName;

          if (param.method instanceof Method)
          {
            methodName = JavaNameHelper.getUniqueMethodName(param.method.getDeclaringClass().getName(),
                    param.method.getName(), ((java.lang.reflect.Method) param.method).getParameterTypes());
          }
          else
          {
            methodName = JavaNameHelper.getUniqueMethodName(param.method.getDeclaringClass().getName(), "<init>",
                    ((Constructor) param.method).getParameterTypes());
          }

          MethodHookConfig methodHookConfig = Registry.getMethodHookConfig(methodName);
          if (methodHookConfig == null) {
            //Log.d(TAG, "beforeHookedMethod: methodhookconfig is null - method: " +  methodName);
            long identifier = idGen.getID();
            stackmap.get(currentThreadID).push(identifier);
            stackmap.get(Thread.currentThread().getId()).push(identifier);
            TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, getParametersInJSONStructure(param.args), identifier,                    prevID, haveID);
            param.setObjectExtra("magnumCallIdentifier", identifier);
            postTransportObject(msg);
          }
          else {
            //Log.d(TAG, "beforeHookedMethod: in else of methodhookconfig - method: " +  methodName);
            ////Log.d(TAG, methodHookConfig.toJSonString());
            if (methodHookConfig.getType() == MethodHookConfig.NONE)
              return; // this should never happen
            else if (methodHookConfig.getType() == MethodHookConfig.TRIGGER) {
              if (Registry.addInvocation(methodName) >= methodHookConfig.getNumInvocations()) {
                Registry.clearMethodHookConfig(methodName);
                long identifier = idGen.getID();
                stackmap.get(currentThreadID).push(identifier);
                TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, getParametersInJSONStructure(param.args), identifier,
                        prevID, haveID);
                param.setObjectExtra("magnumCallIdentifier", identifier);
                postTransportObject(msg);
              }
            }
          }
        } else {
          //Log.d(TAG, "beforeHookedMethod: not triggered: " +  param.method.getName());
          long identifier = idGen.getID();
          stackmap.get(currentThreadID).push(identifier);
          try {

            TransportObject msg = TransportObjectBuilder.buildForMethodEntry(param.method, getParametersInJSONStructure(param.args), identifier,
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
        Log.e(TAG, "afterHookedMethod method:" + param.method.getName());


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

        JSONObject jParameters = new JSONObject();

        if(param.getResult() != null)
        {
          //Log.d(TAG, "Return value " + param.getResult().toString());
          jParameters = jParameters.put(param.getResult().getClass().getCanonicalName(),  param.getResult().toString());
        }


        TransportObject msg = TransportObjectBuilder.buildForMethodExit(param.method, jParameters, identifier);

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
    ////Log.d(Constants.TAG, "Unhooking everything!");
    for (MethodHook hook : Registry.getHooks()) {
      hook.callback.unhook();
      ////Log.d(TAG, "unhooking " + hook.method.getDeclaringClass() + "." + hook.method.getName());
    }

    Registry.clearHooks();
  }

  public void shutdown() {
    unhookAll();
    messageQueue.clear();
    bridge.disconnect();
  }
}
