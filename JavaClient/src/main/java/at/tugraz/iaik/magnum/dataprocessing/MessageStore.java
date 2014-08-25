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
package at.tugraz.iaik.magnum.dataprocessing;

import java.util.concurrent.BlockingQueue;

import at.tugraz.iaik.magnum.data.transport.TransportObject;

public abstract class MessageStore implements IMessageStore {
  private BlockingQueue<PrioritizedTransportObject> queue;

  public MessageStore(BlockingQueue<PrioritizedTransportObject> queue) {
    this.queue = queue;
  }

  @Override
  public void put(TransportObject msg) {
    try {
      queue.put(new PrioritizedTransportObject(msg));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void put(PrioritizedTransportObject msg) {
    try {
      queue.put(msg);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public PrioritizedTransportObject take() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void reEnqueue(PrioritizedTransportObject msg) {
    put(msg);
  }

  @Override
  public int size() {
    return queue.size();
  }
}
