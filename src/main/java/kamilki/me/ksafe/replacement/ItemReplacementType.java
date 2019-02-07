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

public enum ItemReplacementType {

    NAME (true, false),
    LORE (false, true),
    ALL (true, true),
    NONE (false, false);

    private final boolean replaceName;
    private final boolean replaceLore;

    ItemReplacementType(final boolean replaceName, final boolean replaceLore) {
        this.replaceName = replaceName;
        this.replaceLore = replaceLore;
    }

    public boolean replaceName() {
        return this.replaceName;
    }

    public boolean replaceLore() {
        return this.replaceLore;
    }
    
    public static ItemReplacementType get(final boolean replaceName, final boolean replaceLore) {
        if (replaceName && replaceLore) {
            return ItemReplacementType.ALL;
        }
        
        if (replaceName) {
            return ItemReplacementType.NAME;
        }
        
        if (replaceLore) {
            return ItemReplacementType.LORE;
        }
        
        return ItemReplacementType.NONE;
    }
    
}
