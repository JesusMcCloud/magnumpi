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
package at.tugraz.iaik.magnum.client.net;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import at.tugraz.iaik.magnum.data.transport.PackageConfigTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.dataprocessing.IMessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.IOrphanedMessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.IProcessorFactory;

import com.google.inject.Inject;

public class MessageReceivingStateMachine implements Callable<Boolean> {
  private final ExecutorService    dispatcher;
  private final Communication      communication;
  private IOrphanedMessageConsumer orphanedMessageConsumer;
  private IMessageConsumer         messageConsumer;
  private boolean                  started;
  private final ExecutorService    streamDispatcher;
  private final IProcessorFactory  processorFactory;
  private boolean                  flushed;

  @Inject
  public MessageReceivingStateMachine(Communication communication, IMessageConsumer messageConsumer,
      IOrphanedMessageConsumer orphanedMessageConsumer, IProcessorFactory processorFactory) {

    this.communication = communication;
    this.messageConsumer = messageConsumer;
    this.orphanedMessageConsumer = orphanedMessageConsumer;
    this.processorFactory = processorFactory;

    dispatcher = Executors.newFixedThreadPool(2);
    streamDispatcher = Executors.newSingleThreadExecutor();
  }

  @Override
  public Boolean call() {
    dispatcher.execute(messageConsumer);
    dispatcher.execute(orphanedMessageConsumer);

    started = true;
    flushed = false;

    while (communication.isConnected() && started) {
      if (Thread.interrupted())
        break;

      TransportObject msg = null;

      try {
        Future<TransportObject> streamFuture = streamDispatcher.submit(new ReadStreamCallable(communication));

        // We need to do this to enforce a regular check
        // of Thread.isInterrupted (see above)
        msg = streamFuture.get();
        if (msg instanceof PackageConfigTransportObject)
          flushed = true;
        if(!flushed)
          continue;
      } catch (InterruptedException e) {
        // Shutdown of dispatcher. We're done.
        break;
      } catch (ExecutionException | RejectedExecutionException e) {
        break;
      }

      // System.out.println("COMM reading out: " + msg);

      if (processorFactory.isStartStateMessage(msg)) {
        messageConsumer.consume(msg);
      } else {
        // Must be a message for an existing executor
        orphanedMessageConsumer.consume(msg);
      }
    }

    return true;
  }

  public void finishAndShutdown() {
    if (!started)
      return;

    streamDispatcher.shutdown();
    messageConsumer.shutdown();
    dispatcher.shutdown();

    started = false;
  }
}
