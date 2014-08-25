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
package at.tugraz.iaik.magnum.client.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarFile;

import at.tugraz.iaik.magnum.dataprocessing.IMoustacheClassLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.strobel.assembler.metadata.CompositeTypeLoader;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.PlainTextOutput;

@Singleton
public class MoustacheDecompiler implements IMoustacheDecompiler {
  private final IMoustacheClassLoader moustacheClassLoader;
  private DecompilerSettings decompilerSettings;
  private final Map<String, String> classCache;

  @Inject
  public MoustacheDecompiler(IMoustacheClassLoader moustacheClassLoader) {
    this.moustacheClassLoader = moustacheClassLoader;
    classCache = new HashMap<>();

    decompilerSettings = new DecompilerSettings();
    decompilerSettings.setShowDebugLineNumbers(true);
    decompilerSettings.setIncludeErrorDiagnostics(false);
  }

  @Override
  public void decompile(String uniqueMethodName, Continuation<String> continuation) {
    final String className = getClassName(uniqueMethodName);

    if (classCache.containsKey(className))
      continuation.continueWithResult(classCache.get(className));

    continuation.asyncContinueWithResult(new Callable<String>() {
      @Override
      public String call() throws Exception {
        decompileClass(className);
        return classCache.get(className);
      }
    });
  }

  private String getClassName(String uniqueMethodName) {
    int indexOfSlash = uniqueMethodName.indexOf('/');
    String className = indexOfSlash > 0 ? uniqueMethodName.substring(0, indexOfSlash) : uniqueMethodName;

    className = className.substring(0, className.lastIndexOf('.')).replace(";", "").replace('.', '/');

    return className;
  }

  private ITypeLoader getTypeLoader() {
    Set<JarFile> jars = moustacheClassLoader.getJarFiles();
    List<ITypeLoader> loaders = new ArrayList<>(jars.size());

    for (JarFile jar : jars)
      loaders.add(new JarTypeLoader(jar));

    ITypeLoader[] typeLoaders = loaders.toArray(new ITypeLoader[0]);
    return new CompositeTypeLoader(typeLoaders);
  }

  private synchronized void decompileClass(String className) {
    // Maybe another decompilation for this class was queued (due to `synchronized`) 
    // so check if we do need to do the work again.
    if (classCache.containsKey(className))
      return;
    
    decompilerSettings.setTypeLoader(getTypeLoader());

    Writer writer = new StringWriter();
    ITextOutput out = new PlainTextOutput(writer);

    // DecompilationOptions options = new DecompilationOptions();
    // decompilerSettings.getLanguage().decompileMethod(methodDefinition, out,
    // options);

    Decompiler.decompile(className, out, decompilerSettings);

    String sourceCode = writer.toString();
    classCache.put(className, sourceCode);
  }
}
