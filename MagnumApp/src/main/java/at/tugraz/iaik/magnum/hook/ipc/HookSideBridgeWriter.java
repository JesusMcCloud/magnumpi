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

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import android.net.LocalSocket;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.hook.MessageQueue;
import at.tugraz.iaik.magnum.hook.data.Registry;

public class HookSideBridgeWriter implements Callable<Boolean> {
  private final LocalSocket socket;
  private final MessageQueue messageQueue;

  public HookSideBridgeWriter(final LocalSocket socket, MessageQueue messageQueue) {
    this.socket = socket;
    this.messageQueue = messageQueue;
  }

  @Override
  public Boolean call() {
    Log.d(Constants.TAG, "HOOK Txd START");
    start();
    return true;
  }

  private void start() {
    try {
      ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      
      if (!socket.isConnected())
        return;
      
      int i = 0;
      while (!Thread.interrupted() && socket.isConnected()) {
        try {
          TransportObject obj = messageQueue.take();
          objectStream.writeObject(obj);
          objectStream.flush();

          obj = null;

          if (i % Constants.CLEANUP_INTERVAL == 0) {
            objectStream.reset();
            i = 0;
          }

          ++i;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      objectStream.close();
    } catch (Exception e) {
      Log.e("MAGNUM", "Exeption in HSBW!");
      e.printStackTrace();
      Registry.getHook().shutdown();
    }
  }
}
