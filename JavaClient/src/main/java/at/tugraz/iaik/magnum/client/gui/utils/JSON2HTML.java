/*******************************************************************************
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
package at.tugraz.iaik.magnum.client.gui.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON2HTML
{
    public static String convert(String json) throws JSONException
    {
        if(json.equals("{}") || json.length() == 0)
            return "";

        json = "[" + json + "]";

        JSONArray array = new JSONArray(json);
        return createList(array);
    }

    private static String createList(JSONArray array) throws JSONException
    {
        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<ul>\n");

        for (int i = 0; i < array.length(); i++)
        {
            htmlString.append("<li>");
            if (array.get(i) instanceof JSONObject)
            {
                Object name = array.getJSONObject(i).names().get(0);
                htmlString.append(name).append(": ");
                htmlString.append(array.getJSONObject(i).opt(name + ""));
            }
            else
            {
                htmlString.append("null");
            }
            htmlString.append("</li>");
        }
        htmlString.append("</ul>");
        return htmlString.toString();
    }
}
