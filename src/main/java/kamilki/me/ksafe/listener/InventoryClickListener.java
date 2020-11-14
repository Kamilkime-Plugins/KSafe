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

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.ItemData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.util.InventoryUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class InventoryClickListener implements Listener {

    private final ConfigData configData;
    private final PluginData pluginData;

    public InventoryClickListener(final ConfigData configData, final PluginData pluginData) {
        this.configData = configData;
        this.pluginData = pluginData;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        if (!event.getView().getTitle().equals(this.configData.inventoryTitle)) {
            return;
        }

        final int slot = event.getSlot();
        if (slot < 0 || slot >= this.configData.inventoryLayout.size()) {
            return;
        }

        final ItemStack baseItem = this.configData.inventoryLayout.get(slot);
        final Set<ItemData> safeItems = this.configData.inventoryItems.get(baseItem);
        final HumanEntity human = event.getWhoClicked();

        boolean hasChanged = false;
        if (event.isLeftClick()) {
            if (this.configData.allowInvSafeWithdrawal) {
                hasChanged = this.withdraw(human, safeItems);
            } else if (this.configData.allowInvSafeDeposit) {
                hasChanged = this.deposit(human, safeItems);
            }
        } else if (event.isRightClick()) {
            if (this.configData.allowInvSafeDeposit) {
                hasChanged = this.deposit(human, safeItems);
            } else if (this.configData.allowInvSafeWithdrawal) {
                hasChanged = this.withdraw(human, safeItems);
            }
        }

        event.setCancelled(true);
        event.setResult(Result.DENY);

        if (hasChanged) {
            human.closeInventory();
            Bukkit.dispatchCommand(human, "ksafe autoreopen");
        }
    }

    private boolean withdraw(final HumanEntity human, final Set<ItemData> safeItems) {
        boolean hasChanged = false;
        final Map<ItemData, Integer> userSafe = this.pluginData.userSafes.get(human.getUniqueId());

        for (final ItemData itemWithdrawal : safeItems) {
            final int limit = this.configData.itemLimits.get(itemWithdrawal);

            final int invAmount = InventoryUtil.getInventoryAmount(human, itemWithdrawal);
            if (!this.configData.withdrawAll && invAmount >= limit) {
                String message = this.configData.cantWithdrawMsg;

                message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemWithdrawal));
                message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limit));
                message = StringUtils.replace(message, "{AMOUNT}", Integer.toString(invAmount));

                human.sendMessage(message);
                continue;
            }

            final int safeAmount = userSafe.getOrDefault(itemWithdrawal, 0);
            if (safeAmount == 0) {
                String message = this.configData.safeEmptyMsg;

                message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemWithdrawal));

                human.sendMessage(message);
                continue;
            }

            final int toSupply = this.configData.withdrawAll ? safeAmount : Math.min(safeAmount, limit - invAmount);
            final int notAdded = InventoryUtil.addToInventory(human, itemWithdrawal, toSupply);

            final int newSafeAmount = userSafe.getOrDefault(itemWithdrawal, 0) - toSupply + notAdded;
            userSafe.put(itemWithdrawal, newSafeAmount);

            this.pluginData.changedUsers.add(human.getUniqueId());

            String message = this.configData.itemsWithdrawnMsg;

            message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemWithdrawal));
            message = StringUtils.replace(message, "{ADDED}", Integer.toString(toSupply - notAdded));
            message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limit));
            message = StringUtils.replace(message, "{OLD-AMOUNT}", Integer.toString(invAmount));
            message = StringUtils.replace(message, "{NEW-AMOUNT}", Integer.toString(invAmount + toSupply - notAdded));
            message = StringUtils.replace(message, "{OLD-SAFE}", Integer.toString(safeAmount));
            message = StringUtils.replace(message, "{NEW-SAFE}", Integer.toString(newSafeAmount));

            human.sendMessage(message);
            hasChanged = true;
        }
        
        return hasChanged;
    }

    private boolean deposit(final HumanEntity human, final Set<ItemData> safeItems) {
        boolean hasChanged = false;
        final Map<ItemData, Integer> userSafe = this.pluginData.userSafes.get(human.getUniqueId());

        for (final ItemData itemDeposit : safeItems) {
            final int limit = this.configData.itemLimits.get(itemDeposit);
            final int invAmount = InventoryUtil.getInventoryAmount(human, itemDeposit);
            
            final int toDeposit = this.configData.depositAll ? invAmount : invAmount - limit;
            if (toDeposit <= 0) {
                String message = this.configData.cantDepositMsg;

                message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemDeposit));
                message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limit));
                message = StringUtils.replace(message, "{AMOUNT}", Integer.toString(invAmount));

                human.sendMessage(message);
                continue;
            }

            InventoryUtil.removeFromInventory(human, itemDeposit, toDeposit);
            
            final int safeAmount = userSafe.getOrDefault(itemDeposit, 0);
            userSafe.put(itemDeposit, safeAmount + toDeposit);
            
            this.pluginData.changedUsers.add(human.getUniqueId());
            
            String message = this.configData.itemsDepositedMsg;

            message = StringUtils.replace(message, "{ITEM}", this.configData.itemNames.get(itemDeposit));
            message = StringUtils.replace(message, "{ADDED}", Integer.toString(toDeposit));
            message = StringUtils.replace(message, "{LIMIT}", Integer.toString(limit));
            message = StringUtils.replace(message, "{OLD-AMOUNT}", Integer.toString(invAmount));
            message = StringUtils.replace(message, "{NEW-AMOUNT}", Integer.toString(invAmount - toDeposit));
            message = StringUtils.replace(message, "{OLD-SAFE}", Integer.toString(safeAmount));
            message = StringUtils.replace(message, "{NEW-SAFE}", Integer.toString(safeAmount + toDeposit));

            human.sendMessage(message);
            hasChanged = true;
        }
        
        return hasChanged;
    }

}
