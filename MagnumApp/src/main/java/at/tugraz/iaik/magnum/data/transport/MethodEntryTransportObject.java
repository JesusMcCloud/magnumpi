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

import android.util.Log;
import org.json.JSONObject;

import java.io.*;
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

    if(params == null)
    {
      transportBuffer.add(new byte[0]);
      Log.w("MAGNUM", "MethodEntryTransportObject:packForTransport params are null!");
      return;
    }

    for (Object param : params) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(buffer);

      if (param != null && !(param instanceof Serializable)) {
        try {
            out.writeObject(((JSONObject) param).toString());

        } catch (InvalidClassException e) {
          out.writeObject("packForTransport entry InvalidClassException ");
          e.printStackTrace();
        } catch (NotSerializableException e) {
          out.writeObject("packForTransport entry NotSerializableException ");
          e.printStackTrace();
        } catch (IOException e) {
          out.writeObject("packForTransport entry IOException ");
          e.printStackTrace();
        }
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
