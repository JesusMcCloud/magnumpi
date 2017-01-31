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
package at.tugraz.iaik.magnum.app.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import android.util.Log;
import at.tugraz.iaik.magnum.app.cmd.Commander;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.cmd.Command;

public class PcCommReader implements Callable<Boolean> {
  private final Socket socket;

  public PcCommReader(final Socket socket) {
    this.socket = socket;
  }

  @Override
  public Boolean call() throws IOException {
    ////Log.d(Constants.TAG, "calling reader");
    consume();
    return true;
  }

  private void consume() {
    try {
      ObjectInputStream stream = new ObjectInputStream(socket.getInputStream());
      Command cmd;
      while ((cmd = (Command) stream.readObject()) != null) {
        ////Log.d(Constants.TAG, "COMM Rxd: " + cmd.getClass().getSimpleName());
        Commander.handleCommand(cmd);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
