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

package kamilki.me.ksafe.replacement;

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.ItemData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.util.ItemUtil;
import kamilki.me.ksafe.util.QuadFunction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemReplacer {

    private static final Map<String, QuadFunction<ItemData, Player, ConfigData, PluginData, String>> REPLACEMENT_PREFIXES;

    static {
        REPLACEMENT_PREFIXES = new HashMap<>();

        REPLACEMENT_PREFIXES.put("{INV-", (itemData, player, configData, pluginData) -> {
            return Integer.toString(ItemUtil.getInventoryAmount(player, itemData));
        });
        
        REPLACEMENT_PREFIXES.put("{LIMIT-", (itemData, player, configData, pluginData) -> {
            return Integer.toString(configData.itemLimits.getOrDefault(itemData, 0));
        });
        
        REPLACEMENT_PREFIXES.put("{SAFE-", (itemData, player, configData, pluginData) -> {
            return Integer.toString(pluginData.userSafes.get(player.getUniqueId()).getOrDefault(itemData, 0));
        });
        
        REPLACEMENT_PREFIXES.put("{WITHDRAW-", (itemData, player, configData, pluginData) -> {
            final int inv = ItemUtil.getInventoryAmount(player, itemData);
            final int limit = configData.itemLimits.getOrDefault(itemData, 0);
            final int safe = pluginData.userSafes.get(player.getUniqueId()).getOrDefault(itemData, 0);
            
            if (configData.withdrawAll) {
                return Integer.toString(safe);
            }
            
            if (safe == 0 || limit - inv <= 0) {
                return "0";
            }
            
            return Integer.toString(Math.min(safe, limit - inv));
        });
        
        REPLACEMENT_PREFIXES.put("{DEPOSIT-", (itemData, player, configData, pluginData) -> {
            final int inv = ItemUtil.getInventoryAmount(player, itemData);
            final int limit = configData.itemLimits.getOrDefault(itemData, 0);
            
            if (configData.depositAll) {
                return Integer.toString(inv);
            }
            
            return Integer.toString(Math.max(0, inv - limit));
        });
    }

    public static String replace(String name, final Player player, final ConfigData configData, final PluginData pluginData) {
        for (final String prefix : REPLACEMENT_PREFIXES.keySet()) {
            int occurence = name.indexOf(prefix);

            while (occurence >= 0) {
                String itemDataName = "";
                for (int i = occurence + prefix.length(); i < name.length(); i++) {
                    final char c = name.charAt(i);
                    if (c == '}') {
                        break;
                    }

                    itemDataName += c;
                }
                
                final ItemData itemData = ItemData.fromString(itemDataName);
                if (itemData != null) {
                    final String replacement = REPLACEMENT_PREFIXES.get(prefix).apply(itemData, player, configData, pluginData);
                    name = StringUtils.replace(name, prefix + itemDataName + "}", replacement);
                }

                occurence = name.indexOf(prefix);
            }
        }

        return name;
    }

    public static List<String> replace(final List<String> lore, final Player player, final ConfigData configData, final PluginData pluginData) {
        final List<String> replacedLore = new ArrayList<>();
        for (final String loreLine : lore) {
            replacedLore.add(replace(loreLine, player, configData, pluginData));
        }

        return replacedLore;
    }

    public static boolean needsReplacement(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        for (final String prefix : REPLACEMENT_PREFIXES.keySet()) {
            if (name.contains(prefix)) {
                return true;
            }
        }

        return false;
    }

    public static boolean needsReplacement(final List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (final String loreLine : lore) {
            if (needsReplacement(loreLine)) {
                return true;
            }
        }

        return false;
    }

    private ItemReplacer() {}

}
