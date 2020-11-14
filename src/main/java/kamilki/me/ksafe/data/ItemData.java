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

package kamilki.me.ksafe.data;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public final class ItemData {

    private static final Set<ItemData> itemDataCache = new HashSet<>();

    private final Material material;
    private final short durability;

    private ItemData(final Material material, final short durability) {
        this.material = material;
        this.durability = durability;
    }

    public Material getMaterial() {
        return this.material;
    }

    public short getDurability() {
        return this.durability;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + this.durability;
        result = prime * result + ((this.material == null) ? 0 : this.material.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final ItemData other = (ItemData) obj;
        if (this.durability != other.durability) {
            return false;
        }

        return this.material == other.material;
    }
    
    @Override
    public String toString() {
        return this.material.toString() + ":" + this.durability;
    }

    public static ItemData get(final Material material, final short durability) {
        for (final ItemData itemData : itemDataCache) {
            if (itemData.material != material) {
                continue;
            }
            
            if (itemData.durability != durability) {
                continue;
            }
            
            return itemData;
        }
        
        final ItemData itemData = new ItemData(material, durability);
        itemDataCache.add(itemData);
        
        return itemData;
    }
    
    public static ItemData fromString(final String dataString) {
        final String[] dataSplit = dataString.split(":");
        
        final Material material = Material.matchMaterial(dataSplit[0]);
        if (material == null) {
            Bukkit.getLogger().warning("[ItemParser] No material found with name " + dataSplit[0].toUpperCase());
            return null;
        }
        
        final short durability;
        try {
            durability = dataSplit.length == 1 ? 0 : Short.parseShort(dataSplit[1]);
        } catch (final NumberFormatException exception) {
            Bukkit.getLogger().warning("[ItemParser] " + dataSplit[1] + " is not a valid integer!");
            return null;
        }
        
        return get(material, durability);
    }

}
