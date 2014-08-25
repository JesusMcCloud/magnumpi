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
package at.tugraz.iaik.magnum.app;

import java.util.Random;

public abstract class MagnumQuotes {

  private static final Random rand = new Random();
  private static final String[] quotes = {
    "Magnum: I'm not an intruder, I'm a guest!",
    "I'm trying to tie up the loose ends, Floyd, you know, like those TV detectives.",
    "I knew this investigative business was easy, else you wouldn't be in it.",
    "I'm a peach of a detective.",
    "Time has nothing to do with infinity and jelly donuts.",

  };

  public static final String getQuote() {
    return quotes[rand.nextInt(quotes.length)];
  }
}
