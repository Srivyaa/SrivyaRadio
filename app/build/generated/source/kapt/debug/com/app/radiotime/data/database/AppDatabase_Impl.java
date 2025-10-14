package com.app.radiotime.data.database;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EntityDao _entityDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(1, "3c4551dfdd5dad9ddad2ffd023cdb480", "d4458f84a5c8231d34e712128c14f328") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `radio_stations` (`id` TEXT NOT NULL, `favicon` TEXT NOT NULL DEFAULT '', `name` TEXT NOT NULL DEFAULT '', `country` TEXT NOT NULL DEFAULT '', `tags` TEXT NOT NULL DEFAULT '0', `countrycode` TEXT NOT NULL DEFAULT '', `url_resolved` TEXT NOT NULL DEFAULT '', `state` TEXT NOT NULL DEFAULT '', `homepage` TEXT NOT NULL DEFAULT '', `rank` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `saved_items` (`favId` INTEGER PRIMARY KEY AUTOINCREMENT, `id` TEXT NOT NULL, `order` INTEGER)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3c4551dfdd5dad9ddad2ffd023cdb480')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `radio_stations`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `saved_items`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsRadioStations = new HashMap<String, TableInfo.Column>(10);
        _columnsRadioStations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("favicon", new TableInfo.Column("favicon", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("name", new TableInfo.Column("name", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("country", new TableInfo.Column("country", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, "'0'", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("countrycode", new TableInfo.Column("countrycode", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("url_resolved", new TableInfo.Column("url_resolved", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("state", new TableInfo.Column("state", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("homepage", new TableInfo.Column("homepage", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsRadioStations.put("rank", new TableInfo.Column("rank", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysRadioStations = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesRadioStations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRadioStations = new TableInfo("radio_stations", _columnsRadioStations, _foreignKeysRadioStations, _indicesRadioStations);
        final TableInfo _existingRadioStations = TableInfo.read(connection, "radio_stations");
        if (!_infoRadioStations.equals(_existingRadioStations)) {
          return new RoomOpenDelegate.ValidationResult(false, "radio_stations(com.app.radiotime.data.models.Station).\n"
                  + " Expected:\n" + _infoRadioStations + "\n"
                  + " Found:\n" + _existingRadioStations);
        }
        final Map<String, TableInfo.Column> _columnsSavedItems = new HashMap<String, TableInfo.Column>(3);
        _columnsSavedItems.put("favId", new TableInfo.Column("favId", "INTEGER", false, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedItems.put("id", new TableInfo.Column("id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedItems.put("order", new TableInfo.Column("order", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysSavedItems = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesSavedItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavedItems = new TableInfo("saved_items", _columnsSavedItems, _foreignKeysSavedItems, _indicesSavedItems);
        final TableInfo _existingSavedItems = TableInfo.read(connection, "saved_items");
        if (!_infoSavedItems.equals(_existingSavedItems)) {
          return new RoomOpenDelegate.ValidationResult(false, "saved_items(com.app.radiotime.data.models.Favorite).\n"
                  + " Expected:\n" + _infoSavedItems + "\n"
                  + " Found:\n" + _existingSavedItems);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "radio_stations", "saved_items");
  }

  @Override
  public void clearAllTables() {
    super.performClear(false, "radio_stations", "saved_items");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EntityDao.class, EntityDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public EntityDao radioStationDao() {
    if (_entityDao != null) {
      return _entityDao;
    } else {
      synchronized(this) {
        if(_entityDao == null) {
          _entityDao = new EntityDao_Impl(this);
        }
        return _entityDao;
      }
    }
  }
}
