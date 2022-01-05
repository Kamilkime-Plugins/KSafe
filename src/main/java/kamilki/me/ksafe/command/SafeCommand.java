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

package kamilki.me.ksafe.command;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        if (args.length != 0 && reloadArgs.contains(args[0].toLowerCase()) && sender.hasPermission("ksafe.reload")) {
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
            final ItemReplacementType replacementType = this.configData.replaceableItems.get(item);
            if (replacementType == ItemReplacementType.NONE) {
                inventoryItems.add(item);
                continue;
            }

            final ItemStack clone = item.clone();
            final ItemMeta cloneMeta = clone.getItemMeta();

            if (cloneMeta != null) {
                if (replacementType.replaceName()) {
                    cloneMeta.setDisplayName(ItemReplacer.replace(cloneMeta.getDisplayName(), player, this.configData, this.pluginData));
                }

                if (replacementType.replaceLore()) {
                    cloneMeta.setLore(ItemReplacer.replace(cloneMeta.getLore(), player, this.configData, this.pluginData));
                }

                clone.setItemMeta(cloneMeta);
            }

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
