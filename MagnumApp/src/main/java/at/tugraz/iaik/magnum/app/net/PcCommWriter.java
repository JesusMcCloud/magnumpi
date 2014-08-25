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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.hook.MessageQueue;

public class PcCommWriter implements Callable<Boolean> {
  private final Socket socket;
  private final MessageQueue queue;

  public PcCommWriter(final Socket socket, final MessageQueue queue) {
    this.socket = socket;
    this.queue = queue;
  }

  @Override
  public Boolean call() throws IOException {
    consume();
    return true;
  }

  private void consume() throws IOException {
    int messageCounter = 0;
    BufferedOutputStream bufferedStream = new BufferedOutputStream(socket.getOutputStream());
    ObjectOutputStream stream = new ObjectOutputStream(bufferedStream);

    TransportObject msg;

    while (!Thread.interrupted()) {

      try {
        msg = queue.take();
        messageCounter++;
      } catch (InterruptedException e) {
        break;
      }

      if (socket == null) {
        break;
      }

      try {
        stream.writeObject(msg);
      } catch (IOException e) {
        queue.reEnqueue(msg);
      }

      stream.flush();

      if (messageCounter % Constants.CLEANUP_INTERVAL == 0) {
        // ObjectStream leaks a lot of memory to
        // keep track of circular references.
        // If we do not reset() the stream regularly,
        // we experience OutOfMemory on the device.
        stream.reset();
        messageCounter = 0;
      }
    }
  }
}
