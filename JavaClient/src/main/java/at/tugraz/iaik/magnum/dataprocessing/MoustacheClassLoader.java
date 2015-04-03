/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd PrÃ¼nster
 * Copyright 2013, 2014 Bernd PrÃ¼nster
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
package at.tugraz.iaik.magnum.dataprocessing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.jar.JarFile;

import at.tugraz.iaik.magnum.model.ApkModel;

import com.google.inject.Singleton;
import com.googlecode.dex2jar.v3.Dex2jar;

@Singleton
public class MoustacheClassLoader implements IMoustacheClassLoader {
  private final ClassLoader contextClassLoader;
  private final Semaphore blocker;
  private boolean isInitialized;
  private final Set<JarFile> jarFiles;

  public MoustacheClassLoader() throws IOException {
    contextClassLoader = ClassLoader.getSystemClassLoader();
    blocker = new Semaphore(0);
    jarFiles = new HashSet<>();

    initializeDependencies();
  }

  @Override
  public void initializeForApk(ApkModel apk) throws IOException {

      //blocker.acquire();

      File apkFromJar = File.createTempFile("magnum", ".jar");
      apkFromJar.deleteOnExit();

      Dex2jar d2j = Dex2jar.from(apk.getApk());
      d2j.skipDebug();
      d2j.to(apkFromJar);
      addLoaderFromJar(apkFromJar);

      isInitialized = true;
      blocker.release();

      File tmpApk = File.createTempFile("magnum_apk_", ".apk");
      tmpApk.deleteOnExit();

      try (FileOutputStream fos = new FileOutputStream(tmpApk)) {
        fos.write(apk.getApk());
      }
  }

  @Override
  public void waitForInitialization() throws InterruptedException {
    if (!isInitialized)
      blocker.acquire();
  }
  
  public void ApkFileImport(File apkFile) throws IOException {
	  
	  addLoaderFromJar(apkFile);
	  
      isInitialized = true;
      blocker.release();
  }

  private void initializeDependencies() throws IOException {
    File androidJar = jarResource2TmpFile("/at/tugraz/iaik/magnum/client/res/blobs/android.jar");

    addLoaderFromJar(androidJar);
  }

  private URL createUrl(File jar) throws IOException {
    return new URL("jar:file:" + jar.getCanonicalPath() + "!/");
  }

  private File jarResource2TmpFile(String res) throws IOException {
    String[] fname = res.split("/");
    File tmpFile = File.createTempFile("magnum_" + 
    		fname[fname.length - 1].replace(".", ""), "magnum.jar");
    
    tmpFile.deleteOnExit();

    FileOutputStream outputStream = new FileOutputStream(tmpFile);
    InputStream resourceStream = getClass().getResourceAsStream(res);

    BufferedInputStream in = new BufferedInputStream(resourceStream);

    byte[] buf = new byte[1024];
    int read = 0;
    while ((read = in.read(buf)) != -1) {
      outputStream.write(buf, 0, read);
    }

    outputStream.close();
    return tmpFile;
  }

  private void addLoaderFromJar(File jarFile) throws IOException {
    URL jarUrls = createUrl(jarFile);
    URLClassLoader.newInstance(new URL[] { jarUrls }, contextClassLoader);

    jarFiles.add(new JarFile(jarFile, false));
  }

  @Override
  public Set<JarFile> getJarFiles() {
    return jarFiles;
  }
}
