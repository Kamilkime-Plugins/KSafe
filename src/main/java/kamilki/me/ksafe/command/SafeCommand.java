
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
package kamilki.me.ksafe.command;

import com.google.common.collect.Sets;
import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.replacement.ItemReplacementType;
import kamilki.me.ksafe.replacement.ItemReplacer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SafeCommand implements CommandExecutor {

    private static final Set<String> reloadArgs = Sets.newHashSet("reload", "rl");

    private final ConfigData configData;
    private final PluginData pluginData;

    public SafeCommand(final ConfigData configData, final PluginData pluginData) {
        this.configData = configData;
        this.pluginData = pluginData;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length != 0 && sender.hasPermission("ksafe.reload") && reloadArgs.contains(args[0].toLowerCase())) {
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

            this.configData.loadConfig(true);

            sender.sendMessage(this.configData.cfgReloadedMsg);
            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        final Player player = (Player) sender;

        final List<ItemStack> inventoryItems = new ArrayList<>();
        for (final ItemStack item : this.configData.inventoryLayout) {
            final ItemReplacementType replacementType = this.configData.replacableItems.get(item);
            if (replacementType == ItemReplacementType.NONE) {
                inventoryItems.add(item);
                continue;
            }

            final ItemStack clone = item.clone();
            final ItemMeta cloneMeta = clone.getItemMeta();

            if (replacementType.replaceName()) {
                cloneMeta.setDisplayName(ItemReplacer.replace(cloneMeta.getDisplayName(), player, this.configData, this.pluginData));
            }

            if (replacementType.replaceLore()) {
                cloneMeta.setLore(ItemReplacer.replace(cloneMeta.getLore(), player, this.configData, this.pluginData));
            }

            clone.setItemMeta(cloneMeta);
            inventoryItems.add(clone);
        }

        final Inventory safeInventory = Bukkit.createInventory(null, inventoryItems.size(), this.configData.inventoryTitle);
        for (int slot = 0; slot < safeInventory.getSize(); slot++) {
            safeInventory.setItem(slot, inventoryItems.get(slot));
        }
        
        player.openInventory(safeInventory);
        return true;
    }

}
