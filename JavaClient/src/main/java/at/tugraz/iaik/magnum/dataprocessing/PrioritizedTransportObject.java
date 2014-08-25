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

import at.tugraz.iaik.magnum.data.transport.DonePatchingClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.LoadedClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodEntryTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodExitTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodHookTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;

public class PrioritizedTransportObject implements Comparable<PrioritizedTransportObject> {  
  private final TransportObject originalMessage;
  private final short priority;

  public PrioritizedTransportObject(TransportObject msg) {
    this.originalMessage = msg;
    priority = getPriorityClass(msg);
  }

  private static short getPriorityClass(TransportObject msg) {
    if (msg instanceof LoadedClassTransportObject)
      return 10;
    
    if (msg instanceof MethodHookTransportObject)
      return 20;
    
    if (msg instanceof DonePatchingClassTransportObject)
      return 30;
    
    if (msg instanceof MethodEntryTransportObject)
      return 50;
    
    if (msg instanceof MethodExitTransportObject)
      return 60;
    
    return 0;
  }

  @Override
  public int compareTo(PrioritizedTransportObject other) {
    return Short.compare(priority, other.priority);
  }

  public TransportObject getOriginalMessage() {
    return originalMessage;
  }
}
