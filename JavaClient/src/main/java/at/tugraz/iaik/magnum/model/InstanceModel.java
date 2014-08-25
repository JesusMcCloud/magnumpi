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
package at.tugraz.iaik.magnum.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.xml.bind.annotation.XmlElement;

import at.tugraz.iaik.magnum.client.gui.widgets.tree.FormattedInstanceNodeFactory;

public class InstanceModel extends ModelObject {
  @XmlElement
  private final TypeModel type;

  @XmlElement
  private final byte[]    blobData;

  @XmlElement
  private final String    asString;

  private Object          data;

  public InstanceModel(final TypeModel type, final byte[] data) {
    this.type = type;
    blobData = data;
    unpack();
    asString = FormattedInstanceNodeFactory.toString(this.data);
  }

  public TypeModel getType() {
    return type;
  }

  public byte[] getBlob() {
    return blobData;
  }

  public Object getData() {
    return data;
  }

  private void unpack() {
    if (blobData == null)
      return;

    try {
      ByteArrayInputStream buffer = new ByteArrayInputStream(blobData);
      ObjectInputStream objectStream = new ObjectInputStream(buffer);

      data = objectStream.readObject();
      objectStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // Well, FUCK
      System.err.println(e.getMessage());
    }
  }
}
