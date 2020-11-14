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

package kamilki.me.ksafe.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

public final class ItemUtil {

    public static ItemStack parseItem(final ConfigurationSection itemSection) {
        final String[] typeSplit = itemSection.getString("type").split(":");

        Material type = Material.matchMaterial(typeSplit[0]);
        if (type == null) {
            type = Material.BARRIER;
            Bukkit.getLogger().warning("[ItemParser] No material found with name " + typeSplit[0].toUpperCase());
        }

        short durability;
        try {
            durability = typeSplit.length == 1 ? 0 : Short.parseShort(typeSplit[1]);
        } catch (final NumberFormatException exception) {
            durability = 0;
            Bukkit.getLogger().warning("[ItemParser] " + typeSplit[1] + " is not a valid integer!");
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
                final String[] enchantSplit = enchant.split(" ");

                final Enchantment enchantment = Enchantment.getByName(enchantSplit[0].toUpperCase());
                if (enchantment == null) {
                    Bukkit.getLogger().warning("[ItemParser] No enchantment found with name " + enchantSplit[0].toUpperCase());
                    continue;
                }

                int level;
                try {
                    level = enchantSplit.length == 1 ? 1 : Integer.parseInt(enchantSplit[1]);
                } catch (final NumberFormatException exception) {
                    level = 1;
                    Bukkit.getLogger().warning("[ItemParser] " + enchantSplit[1] + " is not a valid integer!");
                }

                parsedMeta.addEnchant(enchantment, level, true);
            }
        }

        parsed.setItemMeta(parsedMeta);
        return parsed;
    }

    public static MaterialData getMaterialData(final String dataString) {
        final String[] dataSplit = dataString.split(":");

        Material material = Material.matchMaterial(dataSplit[0]);
        if (material == null) {
            material = Material.BARRIER;
            Bukkit.getLogger().warning("[ItemParser] No material found with name " + dataSplit[0].toUpperCase());
        }

        byte durability;
        try {
            durability = dataSplit.length == 1 ? 0 : Byte.parseByte(dataSplit[1]);
        } catch (final NumberFormatException exception) {
            durability = 0;
            Bukkit.getLogger().warning("[ItemParser] " + dataSplit[1] + " is not a valid integer!");
        }

        return new MaterialData(material, durability);
    }

    private ItemUtil() {}

}
