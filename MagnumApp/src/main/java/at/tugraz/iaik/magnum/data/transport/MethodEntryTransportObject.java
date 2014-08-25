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
package at.tugraz.iaik.magnum.data.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

public class MethodEntryTransportObject extends MethodInfoTransportObject {
  private static final long serialVersionUID = -1540879350557241194L;

  private List<byte[]>      transportBuffer;
  private final long        identifier;
  private final long        prevID;
  private final boolean     callerKnown;

  MethodEntryTransportObject(final Member method, final Object[] params, final long identifier, final long prevID,
      final boolean callerKnown) {
    super(method);
    this.identifier = identifier;
    this.prevID = prevID;
    this.callerKnown = callerKnown;

    try {
      packForTransport(params);
    } catch (IOException e) {
      // TODO better error handling
      e.printStackTrace();
    }
  }

  private void packForTransport(Object[] params) throws IOException {
    transportBuffer = new ArrayList<byte[]>();

    for (int i = 0; i < params.length; ++i) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(buffer);

      Object o = params[i];
      String packedObjectClassName = "(null)";

      if (o != null && !(o instanceof Serializable)) {
        packedObjectClassName = o.getClass().getName();

        try {
          if (o.getClass().getMethod("toString").getDeclaringClass().equals(Object.class))
            out.writeObject("(non-serializable: " + packedObjectClassName + ")");
          else
            out.writeObject(o.toString());
        } catch (NoSuchMethodException e) {

//        } catch (StackOverflowError e) {
//          out.writeObject("(Exception during serialization: `" + e.getMessage() + "`);");
        }
      }
      //} else

        try {
          // Log.d("MAGNUM", "METO(" + getUniqueMethodName() +
          // "): serializing `"
          // + packedObjectClassName + "`");
          out.writeObject(o);
        } catch (NotSerializableException e) {
          out.writeObject("(Exception during serialization: `" + e.getMessage() + "`);");
        }

      out.close();
      transportBuffer.add(buffer.toByteArray());
    }
  }

  public byte[] getTransportBuffer(int paramNumber) {
    return transportBuffer.get(paramNumber);
  }

  @Override
  public String toString() {
    return Long.toString(getTimestamp()) + ": MethodEntry: " + getUniqueMethodName() + ", CallId: " + identifier;
  }

  public long getIdentifier() {
    return identifier;
  }

  public long getCaller() {
    return prevID;
  }

  public boolean isCallerKnwon() {
    return callerKnown;
  }
}
