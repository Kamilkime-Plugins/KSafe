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

package kamilki.me.ksafe.listener;

import kamilki.me.ksafe.data.PluginData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerJoinListener implements Listener {

    private final PluginData pluginData;
    
    public PlayerJoinListener(final PluginData pluginData) {
        this.pluginData = pluginData;
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        this.pluginData.userSafes.putIfAbsent(event.getPlayer().getUniqueId(), new ConcurrentHashMap<>());
    }
    
}
