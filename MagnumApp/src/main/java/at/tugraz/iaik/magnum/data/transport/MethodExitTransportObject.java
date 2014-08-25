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

public class MethodExitTransportObject extends MethodInfoTransportObject {
  private static final long serialVersionUID = 9199772846151938849L;

  private byte[]            transportBuffer;
  private final long        identifier;

  public MethodExitTransportObject(final Member method, final Object result, final Long identifier) {
    super(method);
    this.identifier = identifier;

    try {
      packForTransport(result);
    } catch (IOException e) {
      // TODO better error handling
      e.printStackTrace();
    }
  }

  void packForTransport(Object result) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    try {
      if (result == null) {
        out.writeObject(result);
        return;
      }

      if (result instanceof Serializable) {
        try{
          out.writeObject(result);
          
        }catch(StackOverflowError stockOVerflow){
          out.writeObject("StackOverflow on serializing "+result.getClass().getCanonicalName());
        }
      } else {
        try {
          if (result.getClass().getMethod("toString").getDeclaringClass().equals(Object.class))
            out.writeObject("(non-serializable: " + result.getClass().getName() + ")");
          else
            out.writeObject(result.toString());
        } catch (NoSuchMethodException e) {
        }
      }
    } catch (NotSerializableException e) {
      out.writeObject("(Exception during serialization: `" + e.getMessage() + "`);");
    }

    out.close();

    transportBuffer = buffer.toByteArray();
  }

  public byte[] getTransportBuffer() {
    return transportBuffer;
  }

  @Override
  public String toString() {
    return Long.toString(getTimestamp()) + ": MethodExit: " + getUniqueMethodName() + ", CallId: " + identifier;
  }

  public long getIdentifier() {
    return identifier;
  }
}
