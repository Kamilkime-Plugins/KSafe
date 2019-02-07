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

package kamilki.me.ksafe.task;

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.ItemData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.util.ItemUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Map.Entry;

public class LimitTask extends BukkitRunnable {

    private final ConfigData configData;
    private final PluginData pluginData;
    
    public LimitTask(final ConfigData configData, final PluginData pluginData) {
        this.configData = configData;
        this.pluginData = pluginData;
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("ksafe.bypass.limit")) {
                continue;
            }
            
            boolean hasChanged = false;
            final Map<ItemData, Integer> userSafe = this.pluginData.userSafes.get(player.getUniqueId());
            
            for (final Entry<ItemData, Integer> limit : this.configData.itemLimits.entrySet()) {
                final ItemData itemData = limit.getKey();
                final int limitValue = limit.getValue();
                
                final int invAmount = ItemUtil.getInventoryAmount(player, itemData);
                if (invAmount <= limitValue) {
                    continue;
                }
                
                final int toRemove = invAmount - limitValue;
                ItemUtil.removeFromInventory(player, itemData, toRemove);
                
                final int safeAmount = userSafe.getOrDefault(itemData, 0);
                userSafe.put(itemData, safeAmount + toRemove);
                
                this.pluginData.changedUsers.add(player.getUniqueId());
                
                String message = this.configData.itemsTakenMsg;
                
                message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemData));
                message = StringUtils.replace(message, "{REMOVED}", Integer.toString(toRemove));
                message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limitValue));
                message = StringUtils.replace(message, "{OLD-AMOUNT}", Integer.toString(invAmount));
                message = StringUtils.replace(message, "{NEW-AMOUNT}", Integer.toString(invAmount - toRemove));
                message = StringUtils.replace(message, "{OLD-SAFE}", Integer.toString(safeAmount));
                message = StringUtils.replace(message, "{NEW-SAFE}", Integer.toString(safeAmount + toRemove));
                
                player.sendMessage(message);
                hasChanged = true;
            }
            
            if (hasChanged) {
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
                Bukkit.dispatchCommand(player, "ksafe autoreopen");
            }
        }
    }

}
