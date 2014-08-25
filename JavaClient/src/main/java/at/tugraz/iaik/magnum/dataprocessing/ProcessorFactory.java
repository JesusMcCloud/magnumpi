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

import at.tugraz.iaik.magnum.data.transport.ApkFileTransportObject;
import at.tugraz.iaik.magnum.data.transport.LoadedClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.LogMessageTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodEntryTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodExitTransportObject;
import at.tugraz.iaik.magnum.data.transport.PackageConfigTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.IModelBuilder;

import com.google.inject.Inject;

public class ProcessorFactory implements IProcessorFactory {

  private final IExecutorManager executorManager;

  @Inject
  public ProcessorFactory(IExecutorManager executorManager) {
    this.executorManager = executorManager;
  }

  @Override
  public Processor createProcessor(TransportObject obj, IModelBuilder builder) {
    if (obj instanceof LogMessageTransportObject) {
      return new LogMessageProcessor(obj, executorManager);
    } else if (obj instanceof PackageConfigTransportObject) {
      return new PackageConfigProcessor(obj, executorManager);
    } else if (obj instanceof ApkFileTransportObject) {
      return new ApkFileProcessor(obj, executorManager);
    } else if (obj instanceof MethodEntryTransportObject) {
      return new MethodEntryProcessor(obj, builder, executorManager);
    } else if (obj instanceof MethodExitTransportObject) {
      return new LongRunningMethodExitProcessor(obj, builder, executorManager);
    } else if (obj instanceof LoadedClassTransportObject) {
      return new LoadClassProcessor(obj, builder, executorManager);
    }

    return null;
  }

  @Override
  public boolean isStartStateMessage(TransportObject obj) {
    return (obj instanceof LogMessageTransportObject)
        || (obj instanceof PackageConfigTransportObject)
        || (obj instanceof ApkFileTransportObject)
        || (obj instanceof MethodEntryTransportObject)
        || (obj instanceof LoadedClassTransportObject)
        || ((obj instanceof MethodExitTransportObject) && executorManager
            .callEntryExists(((MethodExitTransportObject) obj).getIdentifier()));
  }
}
