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

package kamilki.me.ksafe.util;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public final class InventoryUtil {

    public static int getInventoryAmount(final HumanEntity human, final Material material) {
        int amount = 0;

        for (final ItemStack item : human.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }

    public static int addToInventory(final HumanEntity human, final Material material, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 0; slot <= 35 && amount > 0; slot++) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            final int freeSpace = material.getMaxStackSize() - item.getAmount();
            if (freeSpace <= 0) {
                continue;
            }

            final int afterAddition = amount - freeSpace;
            if (afterAddition > 0) {
                item.setAmount(item.getAmount() + freeSpace);
            } else {
                item.setAmount(item.getAmount() + amount);
            }

            amount = afterAddition;
        }

        if (amount > 0) {
            final Map<Integer, ItemStack> notAdded = inventory.addItem(new ItemStack(material, amount));
            if (!notAdded.isEmpty()) {
                int newAmount = 0;
                for (final ItemStack notAddedItem : notAdded.values()) {
                    newAmount += notAddedItem.getAmount();
                }

                amount = newAmount;
            } else {
                amount = 0;
            }
        }

        return Math.max(amount, 0);
    }

    public static void removeFromInventory(final HumanEntity human, final Material material, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 35; slot >= 0 && amount > 0; slot--) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            final int afterRemoval = amount - item.getAmount();
            if (afterRemoval < 0) {
                item.setAmount(item.getAmount() - amount);
            } else {
                inventory.setItem(slot, null);
            }

            amount = afterRemoval;
        }
    }

    private InventoryUtil() {}

}
