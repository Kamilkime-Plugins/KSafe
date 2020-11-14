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
