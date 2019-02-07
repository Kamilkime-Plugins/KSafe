/* Copyright (C) 2019 Kamilkime
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kamilki.me.ksafe.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class StringUtil {
    
    public static String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> color(final List<String> messages) {
        final List<String> colored = new ArrayList<>();
        for (final String message : messages) {
            colored.add(color(message));
        }
        
        return colored;
    }

    private StringUtil() {}
    
}
