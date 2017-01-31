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
package at.tugraz.iaik.magnum.client.gui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public interface GuiConstants {
  public static final String DEFAULT_QUERY    = "ORDER BY timestamp ASC LIMIT 1000";
  public static final String PATH_RES_ICONS   = "/at/tugraz/iaik/magnum/client/res/gui/";
  public static final String NAME_ICON_MAGNUM = "magnum_pi.png";

  public static final Icon   ICON_DISCONNECTED = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "disconnected.png"));
  public static final Icon   ICON_CONNECTED   = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "connected.png"));

  public static final Icon   ICON_REFRESH     = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "refresh.png"));
  public static final Icon   ICON_LOG         = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS + "log.png"));
  public static final Icon   ICON_CLASSES     = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "classes.png"));

  public static final Icon   ICON_CG          = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS + "cg.png"));
  public static final Icon   ICON_BW          = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "bwlist.png"));

  public static final Icon   ICON_SRC         = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS + "src.png"));

  public static final Icon   ICON_HOOKS       = new ImageIcon(EvesDropper.class.getResource(PATH_RES_ICONS
                                                  + "checkbox.png"));

}
