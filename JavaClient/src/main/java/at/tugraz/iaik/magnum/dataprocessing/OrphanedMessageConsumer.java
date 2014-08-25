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

import at.tugraz.iaik.magnum.data.transport.TransportObject;

import com.google.inject.Inject;

public class OrphanedMessageConsumer implements IOrphanedMessageConsumer {

  private final IMessageStore store;
  private final IExecutorManager executorManager;

  @Inject
  public OrphanedMessageConsumer(IMessageStore store, IExecutorManager executorManager) {
    this.store = store;
    this.executorManager = executorManager;
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      processMessages();
    }
  }

  private void processMessages() {
    PrioritizedTransportObject prioritizedMsg = store.take();
    TransportObject msg = prioritizedMsg.getOriginalMessage();

    boolean messageWasConsumed = false;

    for (Processor ex : executorManager) {
      try {
        if (!ex.consumeMessage(msg))
          continue;
      } catch (Exception e) {
        //System.out.println(" --- Error during consumeMessag for msg=" + msg + " and ex=" + ex);
        e.printStackTrace();
        
        continue;
      }

      messageWasConsumed = true;
      break;
    }

    if (!messageWasConsumed) {
      // Maybe the corresponding 'start' event
      // was not processed yet, due to async, so
      // let's try again.
      store.reEnqueue(prioritizedMsg);
    }
  }

  @Override
  public void consume(TransportObject msg) {
    store.put(msg);
  }
}
