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

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import at.tugraz.iaik.magnum.model.MethodInvocationModel;

public class InvocationMapXmlAdapter extends XmlAdapter<InvocationMapXmlType, Map<Long, MethodInvocationModel>> {

  @Override
  public Map<Long, MethodInvocationModel> unmarshal(InvocationMapXmlType v) throws Exception {
    throw new Exception("no unmarshalling yet");
  }

  @Override
  public InvocationMapXmlType marshal(Map<Long, MethodInvocationModel> invocations) throws Exception {
    return new InvocationMapXmlType(invocations.values());
  }

}
