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

import kamilki.me.ksafe.KSafe;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PluginData {

    public final Map<UUID, Map<ItemData, Integer>> userSafes = new ConcurrentHashMap<>();
    public final Set<UUID> changedUsers = ConcurrentHashMap.newKeySet();

    private final KSafe plugin;
    private final ConfigData configData;

    public PluginData(final KSafe plugin, final ConfigData configData) {
        this.configData = configData;
        this.plugin = plugin;
    }

    public void loadUsers() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (!this.initTable()) {
                return;
            }

            final String sql = "SELECT * FROM `" + this.configData.tableName + "`;";

            try (final Connection connection = this.configData.database.getConnection();
                            final PreparedStatement stmt = connection.prepareStatement(sql);
                            final ResultSet resultSet = stmt.executeQuery()) {

                while (resultSet.next()) {
                    final UUID user = UUID.fromString(resultSet.getString("uuid"));
                    final Map<ItemData, Integer> userSafe = new ConcurrentHashMap<>();

                    final String[] safeDataSplit = resultSet.getString("safeData").split(";");
                    for (final String safeDataEntry : safeDataSplit) {
                        final String[] entrySplit = safeDataEntry.split("-");
                        userSafe.put(ItemData.fromString(entrySplit[0]), Integer.parseInt(entrySplit[1]));
                    }

                    this.userSafes.put(user, userSafe);
                }
            } catch (final SQLException exception) {
                this.plugin.getLogger().warning("Failed to load user data!");
            }
        });
    }

    public void saveUsers() {
        if (!this.initTable()) {
            return;
        }

        final String sql;
        if (this.configData.usesMysql) {
            sql = "INSERT INTO `" + this.configData.tableName + "` (`uuid`, `safeData`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `safeData`=?;";
        } else {
            sql = "INSERT OR REPLACE INTO `" + this.configData.tableName + "` (`uuid`, `safeData`) VALUES (?, ?);";
        }

        try (final Connection connection = this.configData.database.getConnection();
                        final PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (final UUID user : this.changedUsers) {
                final Map<ItemData, Integer> safe = this.userSafes.get(user);
                final StringBuilder dataBuilder = new StringBuilder();

                for (final Entry<ItemData, Integer> safeEntry : safe.entrySet()) {
                    dataBuilder.append(";").append(safeEntry.getKey().toString()).append("-").append(safeEntry.getValue());
                }

                stmt.setString(1, user.toString());
                stmt.setString(2, dataBuilder.substring(1));
                
                if (this.configData.usesMysql) {
                    stmt.setString(3, dataBuilder.substring(1));
                }

                stmt.executeUpdate();
            }

            this.changedUsers.clear();
        } catch (final SQLException exception) {
            this.plugin.getLogger().warning("Failed to save user data!");
            exception.printStackTrace();
        }
    }

    public void saveUsersAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.saveUsers();
        });
    }

    private boolean initTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS `" + this.configData.tableName
                        + "` (`uuid` VARCHAR(36) NOT NULL, `safeData` TEXT NOT NULL, PRIMARY KEY(uuid));";

        try (final Connection connection = this.configData.database.getConnection();
                        final PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (final SQLException exception) {
            exception.printStackTrace();
            this.plugin.getLogger().warning("Failed to create database table!");
            return false;
        }

        return true;
    }

}
