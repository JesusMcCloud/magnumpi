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

import java.lang.reflect.Method;

public class StaticBlackWhiteList {

  private final Class<?>[] superClassBlacklist  = { Exception.class, Throwable.class };

  private final String[]   classPrefixBlacklist = { "android.util.Log", "android.os.Build", "android.support.v4.view",
      "android.content.res.XmlResourceParser", "android.graphics.drawable", "android.widget",
      "com.android.internal.widget", "android.view.animation", "android.graphics.Typeface", "de.robv.android.xposed",
      "java.lang", "java.util", "java.lang.Class", "java.io.ByteArrayOutputStream", "java.io.ObjectOutputStream",
      "java.io.ObjectStreamClass", "java.io.DataOutputStream", "java.io.BufferedOutputStream", "android.graphics",
      "android.view.View", "android.view.ContextThemeWrapper", "android.view.LayoutInflater",
      "android.view.inputmethod.InputMethodManager", "android.app.Activity", "android.view.MotionEvent",
      "com.android.internal.view", "android.text.method.PasswordTransformationMethod",
      "android.content.ContextWrapper", "android.util.TypedValue", "java.io.IOException", "com.actionbarsherlock",
      "com.google.ads", "com.google.analytics", "com.android.support.v", "android.os.handler", "android.text.",
      "android.support.v", "android.os.Handler", "android.os.Looper", "android.os.Message", "at.tugraz.iaik.",
      "java.io.NotSerializableException", "android.os.Bundle", "android.support.design.widget", "android.os.Parcel"  };

  private final String[]   methodFQNBlacklist   = { "android.content.Context.getCacheDir",
      "android.content.ContextWrapper.getApplicationInfo", "android.widget.TextView.computeScroll",
      "android.widget.TextView.getCompoundPaddingBottom", "android.widget.TextView.getCompoundPaddingLeft",
      "android.widget.TextView.getCompoundPaddingRight", "android.widget.TextView.getCompoundPaddingTop",
      "android.widget.TextView.getExtendedPaddingBottom", "android.widget.TextView.getExtendedPaddingLeft",
      "android.widget.TextView.getExtendedPaddingTop", "android.widget.TextView.getExtendedPaddingRight",
      "android.widget.TextView.getHorizontalOffsetForDrawables", "android.widget.TextView.getSelectionStart",
      "android.widget.TextView.getSelectionEnd", "java.lang.ClassLoader.loadClass", "java.lang.Object.getClass",
      "android.content.Context.obtainStyledAttributes", "java.nio.charset.Charset.isValidCharsetNameCharacter",
      "java.nio.charset.Charset.checkCharsetName", "android.view.View.canHaveDisplayList", "android.view.View.draw",
      "android.view.View.canResolveLayoutDirection", "android.view.View.getRawLayoutDirection",
      "android.view.View.isLayoutDirectionInherited", "android.app.Dialog.onGenericMotionEvent",
      "java.io.OutputStream.flush", "java.io.OutputStream.write", };

  private final String[]   pacakgeBlackLst      = { "com.google.", "com.android.", "com.cyanogenmod.", "android",
      "at.tugraz.iaik."                        };

  private final String[]   methodFQNWhitelist   = { "android.widget.TextView.getText", "java.lang.Class.forName", };

  public boolean isPackageBlackListed(String packageName) {
    for (String p : pacakgeBlackLst)
      if (packageName.startsWith(p) || packageName.equalsIgnoreCase(p))
        return true;

    return false;
  }

  public boolean isClassBlacklisted(final String fqcn) {
    if (fqcn == null)
      return false;
    if (fqcn.equals("java.lang.Thread") || fqcn.equals("java.util.concurrent.ForkJoinWorkerThread")) {
      return false;
    }

    for (String c : classPrefixBlacklist)
      if (fqcn.startsWith(c))
        return true;

    return false;
  }

  private boolean isMethodBlacklisted(final String fqmn) {
    for (String mn : methodFQNBlacklist)
      if (mn.equals(fqmn))
        return true;

    return false;
  }

  private boolean isMethodWhitelisted(final String fqmn) {
    for (String c : methodFQNWhitelist)
      if (c.equals(fqmn))
        return true;

    return false;
  }

  public boolean isBlacklisted(final Method m) {
    String classn = m.getDeclaringClass().getName();
    final String fqmn = classn + "." + m.getName();

    return !isMethodWhitelisted(fqmn) && (isClassBlacklisted(classn) || isMethodBlacklisted(fqmn));
  }

  public boolean isSuperClassBlacklisted(final Class<?> classOfInterest) {
    if (classOfInterest == null)
      return false;
    if (superClassBlacklistContains(classOfInterest))
      return true;
    else
      return isSuperClassBlacklisted(classOfInterest.getSuperclass());
  }

  public boolean isSuperClass(final Class<?> classOfInterest, Class<?> clazz) {
    // Log.d("MAGNUM", "SUPERCLASS: checking "+classOfInterest+" against "+
    // clazz);
    if (classOfInterest == null)
      return false;
    if (classOfInterest.equals(clazz))
      return true;
    else
      return isSuperClass(classOfInterest.getSuperclass(), clazz);
  }

  public boolean isThreadMethodButStart(Method method) {
    Method[] methods = Thread.class.getDeclaredMethods();
    for (Method m : methods) {
      if (m.getName().equals("start"))
        continue;
      else if (m.getName().equals("run"))
        continue;
      if (m.getName().equals(method.getName())) // OVERRIDES!
        return true;
    }
    return false;
  }

  private boolean superClassBlacklistContains(Class<?> superClass) {
    for (Class<?> clazz : superClassBlacklist)
      if (clazz.equals(superClass))
        return true;

    return false;
  }
}
