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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import at.tugraz.iaik.magnum.client.util.UseLargeThreadPool;
import at.tugraz.iaik.magnum.client.util.UsePriorityQueue;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.IModelBuilder;
import at.tugraz.iaik.magnum.model.ModelObject;

import com.google.inject.Inject;


public class MessageConsumer implements IMessageConsumer {

  private IMessageStore store;
  private IModelBuilder modelBuilder;
  private ExecutorService dispatcher;
  private final IEmitListenerRegistry emitterRegistry;
  private final IExecutorManager executorManager;
  private final IProcessorFactory processorFactory;
  
  @Inject
  public MessageConsumer(@UsePriorityQueue IMessageStore store, IEmitListenerRegistry emitterRegistry, 
      IExecutorManager executorManager, IProcessorFactory processorFactory, IModelBuilder modelBuilder,
      @UseLargeThreadPool ExecutorService dispatcher) {
    this.store = store;
    this.emitterRegistry = emitterRegistry;
    this.executorManager = executorManager;
    this.processorFactory = processorFactory;
    this.modelBuilder = modelBuilder;
    this.dispatcher = dispatcher;
  }

  @Override
  public void run() {  
    while (!Thread.interrupted()) {
      processQueue();
    }
  }

  private void processQueue() {
    PrioritizedTransportObject prioritizedMsg = store.take();
    TransportObject msg = prioritizedMsg.getOriginalMessage();
    
    Processor executor = processorFactory.createProcessor(msg, modelBuilder);
    Future<ModelObject> future = dispatcher.submit(executor);
    
    ModelObjectEmitter emitter = new ModelObjectEmitter(executor, future, emitterRegistry, executorManager);
    dispatcher.execute(emitter);
  }

  @Override
  public void shutdown() {
    dispatcher.shutdown();
  }

  @Override
  public void consume(TransportObject msg) {
    store.put(msg);
  }
}
