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

package kamilki.me.ksafe.replacement;

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.PluginData;
import kamilki.me.ksafe.util.InventoryUtil;
import kamilki.me.ksafe.util.ItemUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemReplacer {

    private static final Map<String, ReplacementFunction> REPLACEMENT_PREFIXES;

    static {
        REPLACEMENT_PREFIXES = new HashMap<>();

        REPLACEMENT_PREFIXES.put("{INV-", (materialData, player, configData, pluginData) -> {
            return Integer.toString(InventoryUtil.getInventoryAmount(player, materialData));
        });

        REPLACEMENT_PREFIXES.put("{LIMIT-", (materialData, player, configData, pluginData) -> {
            return Integer.toString(configData.itemLimits.getOrDefault(materialData, 0));
        });

        REPLACEMENT_PREFIXES.put("{SAFE-", (materialData, player, configData, pluginData) -> {
            return Integer.toString(pluginData.userSafes.get(player.getUniqueId()).getOrDefault(materialData, 0));
        });

        REPLACEMENT_PREFIXES.put("{WITHDRAW-", (materialData, player, configData, pluginData) -> {
            final int inv = InventoryUtil.getInventoryAmount(player, materialData);
            final int limit = configData.itemLimits.getOrDefault(materialData, 0);
            final int safe = pluginData.userSafes.get(player.getUniqueId()).getOrDefault(materialData, 0);

            if (configData.withdrawAll) {
                return Integer.toString(safe);
            }

            if (safe == 0 || limit - inv <= 0) {
                return "0";
            }

            return Integer.toString(Math.min(safe, limit - inv));
        });

        REPLACEMENT_PREFIXES.put("{DEPOSIT-", (materialData, player, configData, pluginData) -> {
            final int inv = InventoryUtil.getInventoryAmount(player, materialData);
            final int limit = configData.itemLimits.getOrDefault(materialData, 0);

            if (configData.depositAll) {
                return Integer.toString(inv);
            }

            return Integer.toString(Math.max(0, inv - limit));
        });
    }

    public static String replace(String name, final Player player, final ConfigData configData, final PluginData pluginData) {
        for (final Map.Entry<String, ReplacementFunction> replacement : REPLACEMENT_PREFIXES.entrySet()) {
            final String prefix = replacement.getKey();

            int occurrence = name.indexOf(prefix);
            while (occurrence >= 0) {
                final StringBuilder materialDataName = new StringBuilder();
                for (int i = occurrence + prefix.length(); i < name.length(); i++) {
                    final char c = name.charAt(i);
                    if (c == '}') {
                        break;
                    }

                    materialDataName.append(c);
                }

                final MaterialData materialData = ItemUtil.getMaterialData(materialDataName.toString());
                final String replacementString = replacement.getValue().replace(materialData, player, configData, pluginData);

                name = StringUtils.replace(name, prefix + materialDataName + "}", replacementString);
                occurrence = name.indexOf(prefix);
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

    private ItemReplacer() {
    }

}
