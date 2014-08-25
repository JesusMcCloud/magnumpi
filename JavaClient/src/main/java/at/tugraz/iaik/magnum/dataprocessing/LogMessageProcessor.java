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

import at.tugraz.iaik.magnum.data.transport.LogMessageTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.LogMessageModel;
import at.tugraz.iaik.magnum.model.ModelObject;

public class LogMessageProcessor extends Processor {

  private LogMessageTransportObject lmto;

  LogMessageProcessor(TransportObject obj, IExecutorManager runningExecutors) {
    super(runningExecutors);
    lmto = (LogMessageTransportObject) obj;
  }

  @Override
  public ModelObject callInternal() throws Exception {
    StringBuilder str = new StringBuilder();

    try {
      str.append("LOG: " + lmto.getFormattedMessage() + "\n");
    } catch (IllegalArgumentException e) {
      str.append("Dropped message due to invalid format: ");
      str.append("Format: `" + lmto.getFormat() + "`; ");
      for (String s : lmto.getParams())
        str.append("Param: " + s);

      str.append("\n");
    }
    return new LogMessageModel(str.toString());
  }
}
