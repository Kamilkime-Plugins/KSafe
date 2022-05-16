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

import com.google.common.collect.Lists;
import java.util.Objects;
import kamilki.me.ksafe.data.ConfigData;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryUtil {

    public static int getInventoryAmount(final HumanEntity human, final Material material, final ConfigData configData) {
        return configData.searchSlots.stream()
                .map(slot -> human.getInventory().getItem(slot))
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static int addToInventory(final HumanEntity human, final Material material, int amount, final ConfigData configData) {
        final PlayerInventory inventory = human.getInventory();

        for (final int slot : configData.searchSlots) {
            final ItemStack item = inventory.getItem(slot);

            if (item == null || item.getType() != material) {
                continue;
            }

            final int freeSpace = material.getMaxStackSize() - item.getAmount();
            if (freeSpace <= 0) {
                continue;
            }

            final int afterAddition = amount - freeSpace;
            if (afterAddition <= 0) {
                item.setAmount(item.getAmount() + amount);
                amount = 0;
                break;
            }

            item.setAmount(item.getAmount() + freeSpace);
            amount = afterAddition;
        }

        if (amount > 0) {
            amount = inventory.addItem(new ItemStack(material, amount)).values().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(ItemStack::getAmount)
                    .sum();
        }

        return Math.max(amount, 0);
    }

    public static void removeFromInventory(final HumanEntity human, final Material material, int amount, final ConfigData configData) {
        final PlayerInventory inventory = human.getInventory();

        for (final int slot : Lists.reverse(configData.searchSlots)) {
            final ItemStack item = inventory.getItem(slot);

            if (item == null || item.getType() != material) {
                continue;
            }

            final int afterRemoval = amount - item.getAmount();
            if (afterRemoval < 0) {
                item.setAmount(item.getAmount() - amount);
                break;
            }

            if (afterRemoval == 0) {
                inventory.setItem(slot, null);
                break;
            }

            inventory.setItem(slot, null);
            amount = afterRemoval;
        }
    }

    private InventoryUtil() {}

}
