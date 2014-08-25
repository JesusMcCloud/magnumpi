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
package at.tugraz.iaik.magnum.app.ipc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.Callable;

import android.net.LocalSocket;
import at.tugraz.iaik.magnum.app.net.PcComm;
import at.tugraz.iaik.magnum.data.transport.TransportObject;

public class BridgeReader implements Callable<Boolean> {
  private final LocalSocket socket;
  private String packageName;

  public BridgeReader(final LocalSocket socket, String packageName) {
    this.socket = socket;
    this.packageName= packageName;
  }

  @Override
  public Boolean call() throws IOException {
    start();
    return true;
  }

 
  private void start() throws IOException {
    try {
      BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
      ObjectInputStream objectStream = new ObjectInputStream(bufferedInputStream);
      
      while (!Thread.currentThread().isInterrupted()) {
        TransportObject buffer;
        
        // We close the socket from outside, so the stream breaks
        if ((buffer = (TransportObject) objectStream.readObject()) != null)
          PcComm.getInstance().write(buffer);
      }
    } catch (Exception e) {
      ServiceSideBridge.getInstance().disconnect(packageName);
     
    }
  }
}
