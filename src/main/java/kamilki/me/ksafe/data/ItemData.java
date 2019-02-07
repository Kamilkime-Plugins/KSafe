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

        result = prime * result + durability;
        result = prime * result + ((material == null) ? 0 : material.hashCode());

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

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ItemData other = (ItemData) obj;
        if (durability != other.durability) {
            return false;
        }

        if (material != other.material) {
            return false;
        }

        return true;
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
