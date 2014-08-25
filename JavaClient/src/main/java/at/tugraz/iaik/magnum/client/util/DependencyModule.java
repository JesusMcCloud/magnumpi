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
package at.tugraz.iaik.magnum.client.util;

import java.util.concurrent.ExecutorService;

import at.tugraz.iaik.magnum.client.conf.IRuntimeConfig;
import at.tugraz.iaik.magnum.client.conf.RuntimeConfig;
import at.tugraz.iaik.magnum.client.db.DBUtil;
import at.tugraz.iaik.magnum.client.db.IDBUtil;
import at.tugraz.iaik.magnum.client.net.Communication;
import at.tugraz.iaik.magnum.client.net.NetworkCommunication;
import at.tugraz.iaik.magnum.dataprocessing.DefaultMessageStore;
import at.tugraz.iaik.magnum.dataprocessing.EmitListenerRegistry;
import at.tugraz.iaik.magnum.dataprocessing.ExecutorManager;
import at.tugraz.iaik.magnum.dataprocessing.IEmitListenerRegistry;
import at.tugraz.iaik.magnum.dataprocessing.IExecutorManager;
import at.tugraz.iaik.magnum.dataprocessing.IMessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.IMessageStore;
import at.tugraz.iaik.magnum.dataprocessing.IMoustacheClassLoader;
import at.tugraz.iaik.magnum.dataprocessing.IOrphanedMessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.IProcessorFactory;
import at.tugraz.iaik.magnum.dataprocessing.MessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.MoustacheClassLoader;
import at.tugraz.iaik.magnum.dataprocessing.OrphanedMessageConsumer;
import at.tugraz.iaik.magnum.dataprocessing.PrioritizedMessageStore;
import at.tugraz.iaik.magnum.dataprocessing.ProcessorFactory;
import at.tugraz.iaik.magnum.model.IModelBuilder;
import at.tugraz.iaik.magnum.model.ModelBuilder;

import com.google.inject.AbstractModule;

public class DependencyModule extends AbstractModule {
  @Override
  protected void configure() {
    // bind(interface).to(implementation)

    bind(Communication.class).to(NetworkCommunication.class);
    bind(IExecutorManager.class).to(ExecutorManager.class);
    bind(IProcessorFactory.class).to(ProcessorFactory.class);

    // bindScope(UsePriorityQueue.class, priorityQueueScope);
    bind(IMessageStore.class).to(DefaultMessageStore.class);
    bind(IMessageStore.class).annotatedWith(UsePriorityQueue.class).to(PrioritizedMessageStore.class);
    bind(ExecutorService.class).annotatedWith(UseLargeThreadPool.class).to(MagnumThreadPoolExecutor.class);

    bind(IMessageConsumer.class).to(MessageConsumer.class);
    bind(IOrphanedMessageConsumer.class).to(OrphanedMessageConsumer.class);
    bind(IModelBuilder.class).to(ModelBuilder.class);
    bind(IEmitListenerRegistry.class).to(EmitListenerRegistry.class);
    bind(IMoustacheClassLoader.class).to(MoustacheClassLoader.class);
    bind(IMoustacheDecompiler.class).to(MoustacheDecompiler.class);

    bind(IRuntimeConfig.class).to(RuntimeConfig.class);
    bind(IDBUtil.class).to(DBUtil.class);
  }
}