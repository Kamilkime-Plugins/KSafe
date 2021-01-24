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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemUtil {

    public static ItemStack parseItem(final ConfigurationSection itemSection) {
        final Material material = ItemUtil.getMaterial(itemSection.getString("type"));
        final ItemStack parsed = new ItemStack(material, Math.min(1, itemSection.getInt("amount", 1)));

        final ItemMeta parsedMeta = parsed.getItemMeta();
        if (parsedMeta == null) {
            return parsed;
        }
        
        final String name = itemSection.getString("name", "");
        if (name != null && !name.isEmpty()) {
            parsedMeta.setDisplayName(StringUtil.color(name));
        }

        final List<String> lore = itemSection.getStringList("lore");
        if (!lore.isEmpty()) {
            parsedMeta.setLore(StringUtil.color(lore));
        }

        final List<String> enchants = itemSection.getStringList("enchants");
        if (!enchants.isEmpty()) {
            for (final String enchant : enchants) {
                final String[] enchantSplit = enchant.split(" ");

                final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantSplit[0].toLowerCase()));
                if (enchantment == null) {
                    Bukkit.getLogger().warning("[ItemParser] No enchantment found with key " + enchantSplit[0].toLowerCase());
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

    public static Material getMaterial(final String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return Material.BARRIER;
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.BARRIER;
            Bukkit.getLogger().warning("[ItemParser] No material found with name " + materialName.toUpperCase());
        }

        return material;
    }

    private ItemUtil() {}

}
