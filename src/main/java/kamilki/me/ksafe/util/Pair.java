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

public final class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public L getKey() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    public R getValue() {
        return this.right;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<L, R>(left, right);
    }

}
