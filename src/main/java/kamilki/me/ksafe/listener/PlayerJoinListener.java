/*
 * Copyright (C) 2020 Kamil Trysiński
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
