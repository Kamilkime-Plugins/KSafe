/*
 * Copyright (C) 2021 Kamil Trysiński
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

package kamilki.me.ksafe;

import kamilki.me.ksafe.command.SafeCommand;
import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.listener.InventoryClickListener;
import kamilki.me.ksafe.listener.PlayerJoinListener;
import kamilki.me.ksafe.task.AutoSupplyTask;
import kamilki.me.ksafe.task.LimitTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class KSafe extends JavaPlugin {
    
    private ConfigData configData;
    private PluginData pluginData;
    
    @Override
    public void onEnable() {
        final PluginManager pluginManager = this.getServer().getPluginManager();
        
        this.configData = new ConfigData(this);
        if (!this.configData.initDatabase()) {
            this.getLogger().warning("Database connection failed, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }
        
        this.configData.loadConfig(false);
        
        this.pluginData = new PluginData(this, this.configData);
        this.pluginData.loadUsers();
        
        if (this.configData.enableAutoSave) {
            this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> this.pluginData.saveUsers(),
                    this.configData.autoSaveInterval, this.configData.autoSaveInterval);
        }
        
        if (this.configData.autoLimit) {
            final long limitTaskInterval = this.configData.limitTaskInterval;
            new LimitTask(this.configData, this.pluginData).runTaskTimer(this, limitTaskInterval, limitTaskInterval);
        }
        
        if (this.configData.autoSupply) {
            final long autoSupplyTaskInterval = this.configData.autoSupplyTaskInterval;
            new AutoSupplyTask(this.configData, this.pluginData).runTaskTimer(this, autoSupplyTaskInterval, autoSupplyTaskInterval);
        }
        
        pluginManager.registerEvents(new PlayerJoinListener(this.pluginData), this);
        pluginManager.registerEvents(new InventoryClickListener(this.configData, this.pluginData), this);
        
        Objects.requireNonNull(this.getCommand("ksafe")).setExecutor(new SafeCommand(this.configData, this.pluginData));
        
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.pluginData.userSafes.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
        }
    }
    
    @Override
    public void onDisable() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String title = player.getOpenInventory().getTitle();

            if (!title.equals(this.configData.inventoryTitle)) {
                continue;
            }

            player.closeInventory();
            player.sendMessage(this.configData.reloadInvCloseMsg);
        }
        
        if (this.pluginData != null) {
            this.pluginData.saveUsers();
        }
    }

}
