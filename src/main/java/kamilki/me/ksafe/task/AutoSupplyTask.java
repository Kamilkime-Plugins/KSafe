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

public class AutoSupplyTask extends BukkitRunnable {

    private final ConfigData configData;
    private final PluginData pluginData;
    
    public AutoSupplyTask(final ConfigData configData, final PluginData pluginData) {
        this.configData = configData;
        this.pluginData = pluginData;
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("ksafe.bypass.supply")) {
                continue;
            }
            
            boolean hasChanged = false;
            final Map<ItemData, Integer> userSafe = this.pluginData.userSafes.get(player.getUniqueId());
            
            for (final Entry<ItemData, Integer> limit : this.configData.itemLimits.entrySet()) {
                final ItemData itemData = limit.getKey();
                final int limitValue = limit.getValue();
                
                final int invAmount = ItemUtil.getInventoryAmount(player, itemData);
                if (invAmount >= limitValue) {
                    continue;
                }
                
                final int safeAmount = userSafe.getOrDefault(itemData, 0);
                if (safeAmount == 0) {
                    continue;
                }
                
                final int toSupply = Math.min(safeAmount, limitValue - invAmount);
                final int notAdded = ItemUtil.addToInventory(player, itemData, toSupply);
                
                final int newSafeAmount = userSafe.getOrDefault(itemData, 0) - toSupply + notAdded;
                userSafe.put(itemData, newSafeAmount);
                
                this.pluginData.changedUsers.add(player.getUniqueId());
                
                String message = this.configData.itemsSuppliedMsg;
                
                message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemData));
                message = StringUtils.replace(message, "{ADDED}", Integer.toString(toSupply - notAdded));
                message = StringUtils.replace(message, "{UNDER}", Integer.toString(toSupply));
                message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limitValue));
                message = StringUtils.replace(message, "{OLD-AMOUNT}", Integer.toString(invAmount));
                message = StringUtils.replace(message, "{NEW-AMOUNT}", Integer.toString(invAmount + toSupply - notAdded));
                message = StringUtils.replace(message, "{OLD-SAFE}", Integer.toString(safeAmount));
                message = StringUtils.replace(message, "{NEW-SAFE}", Integer.toString(newSafeAmount));
                
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
