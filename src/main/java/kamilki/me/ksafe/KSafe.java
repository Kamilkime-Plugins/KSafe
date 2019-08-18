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

package kamilki.me.ksafe;

import kamilki.me.ksafe.command.SafeCommand;
import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.listener.InventoryClickListener;
import kamilki.me.ksafe.listener.PlayerJoinListener;
import kamilki.me.ksafe.task.AutoSaveTask;
import kamilki.me.ksafe.task.AutoSupplyTask;
import kamilki.me.ksafe.task.LimitTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
            final long autoSaveInterval = this.configData.autoSaveInterval;
            new AutoSaveTask(this.pluginData).runTaskTimerAsynchronously(this, autoSaveInterval, autoSaveInterval);
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
        
        this.getCommand("ksafe").setExecutor(new SafeCommand(this.configData, this.pluginData));
        
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.pluginData.userSafes.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
        }
    }
    
    @Override
    public void onDisable() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final InventoryView openInventory = player.getOpenInventory();
            if (openInventory == null) {
                continue;
            }

            final String title = openInventory.getTitle();
            if (title == null || title.isEmpty()) {
                continue;
            }

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
