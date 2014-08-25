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
package at.tugraz.iaik.magnum.hook.ipc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.StrictMode;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.hook.MessageQueue;

public class HookSideBridgeSupervisor implements Callable<Boolean> {
  private LocalSocket sock;
  private ExecutorService dispatcher;
  private final MessageQueue q;
  private final String pkgName;
  private boolean running;

  public HookSideBridgeSupervisor(final MessageQueue q, final LocalSocket socket, final String pkgName) {
    StrictMode.enableDefaults();
    this.q = q;
    this.sock = socket;
    this.pkgName = pkgName;
    dispatcher = Executors.newFixedThreadPool(2);
  }

  public Boolean call() {
    start();
    return true;
  }

  public void start() {
    running = true;
    
    while (!Thread.interrupted() && running) {
      try {
        Log.d(Constants.TAG, "HSBS running");
        HookSideBridgeReader reader = new HookSideBridgeReader(sock);
        HookSideBridgeWriter writer = new HookSideBridgeWriter(sock, q);

        ArrayList<Callable<Boolean>> rw = new ArrayList<Callable<Boolean>>(2);
        rw.add(reader);
        rw.add(writer);
        dispatcher.invokeAny(rw);
        
        Log.d(Constants.TAG, "HSBS Done");

        sock.close();
        int tries = 0;
        if (running) {
          do {
            try {
              Thread.sleep(1000);
              sock = new LocalSocket();
              sock.connect(new LocalSocketAddress(Constants.NAME_SOCKET + "." + pkgName));
              break;
            } catch (IOException e) {
              tries++;
            } catch (InterruptedException e) {
              tries++;
            }
            
          } while (running && tries < 10);
        }

      } catch (Exception e) {
        Log.e("MAGNUM", "Exception in HSBS!");
        e.printStackTrace();
      }
    }
  }

  public void disconnect() {
    try {
      running = false;
      dispatcher.shutdownNow();
      sock.close();
    } catch (IOException e) {
    }
  }
}
