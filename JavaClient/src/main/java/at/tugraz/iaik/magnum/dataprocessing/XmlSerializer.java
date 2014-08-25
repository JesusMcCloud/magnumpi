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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

import at.tugraz.iaik.magnum.model.ClassModel;

public class XmlSerializer {

  public static void exportModel(List<ClassModel> classList, String exportPath) throws IOException, JAXBException {
    FileOutputStream fileStream = new FileOutputStream(exportPath);
    JAXBContext jc = JAXBContext.newInstance(Trace.class, ClassModel.class);
    
    Marshaller marshaller = jc.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    
    Trace trace = new Trace(classList);
    
    JAXBElement<Trace> jaxbElement = 
        new JAXBElement<Trace>(new QName("trace"), Trace.class, trace);
    
    marshaller.marshal(jaxbElement, fileStream);
  }
}

class Trace {
  @XmlElement(name = "class")
  private List<ClassModel> classes;

  Trace(List<ClassModel> classes) {
    this.classes = classes;
  }
}