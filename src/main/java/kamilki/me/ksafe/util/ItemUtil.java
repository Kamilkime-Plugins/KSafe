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

package kamilki.me.ksafe.util;

import kamilki.me.ksafe.data.ItemData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class ItemUtil {

    public static ItemStack fromConfigSection(final ConfigurationSection itemSection) {
        final String[] typeSplit = itemSection.getString("type").split(":");
        final Material type = Material.matchMaterial(typeSplit[0]);

        if (type == null) {
            Bukkit.getLogger().warning("[ItemParser] No material found with name " + typeSplit[0].toUpperCase());
            return null;
        }

        final short durability;
        try {
            durability = typeSplit.length == 1 ? 0 : Short.parseShort(typeSplit[1]);
        } catch (final NumberFormatException exception) {
            Bukkit.getLogger().warning("[ItemParser] " + typeSplit[1] + " is not a valid integer!");
            return null;
        }

        final int amount = itemSection.getInt("amount");
        
        final ItemStack parsed = new ItemStack(type, amount <= 0 ? 1 : amount, durability);
        final ItemMeta parsedMeta = parsed.getItemMeta();
        
        final String name = itemSection.getString("name", "");
        if (!name.isEmpty()) {
            parsedMeta.setDisplayName(StringUtil.color(name));
        }

        final List<String> lore = itemSection.getStringList("lore");
        if (lore != null && !lore.isEmpty()) {
            parsedMeta.setLore(StringUtil.color(lore));
        }

        final List<String> enchants = itemSection.getStringList("enchants");
        if (enchants != null && !enchants.isEmpty()) {
            for (final String enchant : enchants) {
                final String[] splitEnch = enchant.split(" ");
                final Enchantment ench = Enchantment.getByName(splitEnch[0].toUpperCase());

                if (ench == null) {
                    Bukkit.getLogger().warning("[ItemParser] No enchantment found with name " + splitEnch[0].toUpperCase());
                    return null;
                }

                parsedMeta.addEnchant(ench, splitEnch.length == 1 ? 1 : Integer.parseInt(splitEnch[1]), true);
            }
        }

        parsed.setItemMeta(parsedMeta);
        return parsed;
    }

    public static int getInventoryAmount(final HumanEntity human, final Material material, final short durability) {
        int amount = 0;

        for (final ItemStack item : human.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }

    public static int getInventoryAmount(final HumanEntity human, final ItemData itemData) {
        return getInventoryAmount(human, itemData.getMaterial(), itemData.getDurability());
    }
    
    public static int addToInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 0; slot <= 35 && amount > 0; slot++) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }
            
            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
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
            final Map<Integer, ItemStack> notAdded = inventory.addItem(new ItemStack(material, amount, durability));
            if (notAdded != null && !notAdded.isEmpty()) {
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
    
    public static int addToInventory(final HumanEntity human, final ItemData itemData, final int amount) {
        return addToInventory(human, itemData.getMaterial(), itemData.getDurability(), amount);
    }

    public static void removeFromInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 35; slot >= 0 && amount > 0; slot--) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }
            
            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
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
    
    public static void removeFromInventory(final HumanEntity human, final ItemData itemData, final int amount) {
        removeFromInventory(human, itemData.getMaterial(), itemData.getDurability(), amount);
    }

    private ItemUtil() {}

}
