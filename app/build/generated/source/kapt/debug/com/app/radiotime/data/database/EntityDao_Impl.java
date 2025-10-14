package com.app.radiotime.data.database;

import androidx.annotation.NonNull;
import androidx.room.AmbiguousColumnResolver;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.app.radiotime.data.models.Favorite;
import com.app.radiotime.data.models.Station;
import java.lang.Class;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class EntityDao_Impl implements EntityDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Station> __insertAdapterOfStation;

  private final EntityInsertAdapter<Favorite> __insertAdapterOfFavorite;

  private final EntityDeleteOrUpdateAdapter<Favorite> __deleteAdapterOfFavorite;

  private final EntityDeleteOrUpdateAdapter<Favorite> __updateAdapterOfFavorite;

  public EntityDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfStation = new EntityInsertAdapter<Station>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `radio_stations` (`id`,`favicon`,`name`,`country`,`tags`,`countrycode`,`url_resolved`,`state`,`homepage`,`rank`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, @NonNull final Station entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getFavicon() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getFavicon());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getName());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getCountry());
        }
        if (entity.getTags() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getTags());
        }
        if (entity.getCountrycode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getCountrycode());
        }
        if (entity.getUrl_resolved() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getUrl_resolved());
        }
        if (entity.getState() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getState());
        }
        if (entity.getHomepage() == null) {
          statement.bindNull(9);
        } else {
          statement.bindText(9, entity.getHomepage());
        }
        statement.bindLong(10, entity.getRank());
      }
    };
    this.__insertAdapterOfFavorite = new EntityInsertAdapter<Favorite>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `saved_items` (`favId`,`id`,`order`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final Favorite entity) {
        if (entity.getFavId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindLong(1, entity.getFavId());
        }
        if (entity.getId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getId());
        }
        if (entity.getOrder() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getOrder());
        }
      }
    };
    this.__deleteAdapterOfFavorite = new EntityDeleteOrUpdateAdapter<Favorite>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `saved_items` WHERE `favId` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final Favorite entity) {
        if (entity.getFavId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindLong(1, entity.getFavId());
        }
      }
    };
    this.__updateAdapterOfFavorite = new EntityDeleteOrUpdateAdapter<Favorite>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `saved_items` SET `favId` = ?,`id` = ?,`order` = ? WHERE `favId` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final Favorite entity) {
        if (entity.getFavId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindLong(1, entity.getFavId());
        }
        if (entity.getId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getId());
        }
        if (entity.getOrder() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getOrder());
        }
        if (entity.getFavId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getFavId());
        }
      }
    };
  }

  @Override
  public Object insertStation(final List<Station> radioStations,
      final Continuation<? super Unit> $completion) {
    if (radioStations == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfStation.insert(_connection, radioStations);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object insertFavoriteItem(final Favorite favoriteStation,
      final Continuation<? super Unit> $completion) {
    if (favoriteStation == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfFavorite.insert(_connection, favoriteStation);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object insertFavoriteItems(final List<Favorite> favoriteStations,
      final Continuation<? super Unit> $completion) {
    if (favoriteStations == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfFavorite.insert(_connection, favoriteStations);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object deleteFavoriteStation(final Favorite favItem,
      final Continuation<? super Unit> $completion) {
    if (favItem == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfFavorite.handle(_connection, favItem);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object deleteFavoriteStations(final List<Favorite> favItems,
      final Continuation<? super Unit> $completion) {
    if (favItems == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfFavorite.handleMultiple(_connection, favItems);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object updateFavoriteItem(final Favorite item,
      final Continuation<? super Unit> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __updateAdapterOfFavorite.handle(_connection, item);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object getStations(final String countryCode,
      final Continuation<? super List<Station>> $completion) {
    final String _sql = "SELECT * FROM radio_stations WHERE countrycode = (?) ORDER BY rank ASC";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (countryCode == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, countryCode);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfFavicon = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "favicon");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfCountry = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "country");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfCountrycode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "countrycode");
        final int _columnIndexOfUrlResolved = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url_resolved");
        final int _columnIndexOfState = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "state");
        final int _columnIndexOfHomepage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "homepage");
        final int _columnIndexOfRank = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rank");
        final List<Station> _result = new ArrayList<Station>();
        while (_stmt.step()) {
          final Station _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpFavicon;
          if (_stmt.isNull(_columnIndexOfFavicon)) {
            _tmpFavicon = null;
          } else {
            _tmpFavicon = _stmt.getText(_columnIndexOfFavicon);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpCountry;
          if (_stmt.isNull(_columnIndexOfCountry)) {
            _tmpCountry = null;
          } else {
            _tmpCountry = _stmt.getText(_columnIndexOfCountry);
          }
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpCountrycode;
          if (_stmt.isNull(_columnIndexOfCountrycode)) {
            _tmpCountrycode = null;
          } else {
            _tmpCountrycode = _stmt.getText(_columnIndexOfCountrycode);
          }
          final String _tmpUrl_resolved;
          if (_stmt.isNull(_columnIndexOfUrlResolved)) {
            _tmpUrl_resolved = null;
          } else {
            _tmpUrl_resolved = _stmt.getText(_columnIndexOfUrlResolved);
          }
          final String _tmpState;
          if (_stmt.isNull(_columnIndexOfState)) {
            _tmpState = null;
          } else {
            _tmpState = _stmt.getText(_columnIndexOfState);
          }
          final String _tmpHomepage;
          if (_stmt.isNull(_columnIndexOfHomepage)) {
            _tmpHomepage = null;
          } else {
            _tmpHomepage = _stmt.getText(_columnIndexOfHomepage);
          }
          final int _tmpRank;
          _tmpRank = (int) (_stmt.getLong(_columnIndexOfRank));
          _item = new Station(_tmpId,_tmpFavicon,_tmpName,_tmpCountry,_tmpTags,_tmpCountrycode,_tmpUrl_resolved,_tmpState,_tmpHomepage,_tmpRank);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getStationById(final String id, final Continuation<? super Station> $completion) {
    final String _sql = "SELECT * FROM radio_stations WHERE id = (?)";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfFavicon = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "favicon");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfCountry = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "country");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfCountrycode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "countrycode");
        final int _columnIndexOfUrlResolved = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url_resolved");
        final int _columnIndexOfState = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "state");
        final int _columnIndexOfHomepage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "homepage");
        final int _columnIndexOfRank = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rank");
        final Station _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpFavicon;
          if (_stmt.isNull(_columnIndexOfFavicon)) {
            _tmpFavicon = null;
          } else {
            _tmpFavicon = _stmt.getText(_columnIndexOfFavicon);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpCountry;
          if (_stmt.isNull(_columnIndexOfCountry)) {
            _tmpCountry = null;
          } else {
            _tmpCountry = _stmt.getText(_columnIndexOfCountry);
          }
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpCountrycode;
          if (_stmt.isNull(_columnIndexOfCountrycode)) {
            _tmpCountrycode = null;
          } else {
            _tmpCountrycode = _stmt.getText(_columnIndexOfCountrycode);
          }
          final String _tmpUrl_resolved;
          if (_stmt.isNull(_columnIndexOfUrlResolved)) {
            _tmpUrl_resolved = null;
          } else {
            _tmpUrl_resolved = _stmt.getText(_columnIndexOfUrlResolved);
          }
          final String _tmpState;
          if (_stmt.isNull(_columnIndexOfState)) {
            _tmpState = null;
          } else {
            _tmpState = _stmt.getText(_columnIndexOfState);
          }
          final String _tmpHomepage;
          if (_stmt.isNull(_columnIndexOfHomepage)) {
            _tmpHomepage = null;
          } else {
            _tmpHomepage = _stmt.getText(_columnIndexOfHomepage);
          }
          final int _tmpRank;
          _tmpRank = (int) (_stmt.getLong(_columnIndexOfRank));
          _result = new Station(_tmpId,_tmpFavicon,_tmpName,_tmpCountry,_tmpTags,_tmpCountrycode,_tmpUrl_resolved,_tmpState,_tmpHomepage,_tmpRank);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getStationByName(final String search,
      final Continuation<? super List<Station>> $completion) {
    final String _sql = "SELECT * FROM radio_stations WHERE name LIKE '%' || ? || '%' COLLATE NOCASE";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (search == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, search);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfFavicon = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "favicon");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfCountry = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "country");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfCountrycode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "countrycode");
        final int _columnIndexOfUrlResolved = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url_resolved");
        final int _columnIndexOfState = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "state");
        final int _columnIndexOfHomepage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "homepage");
        final int _columnIndexOfRank = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rank");
        final List<Station> _result = new ArrayList<Station>();
        while (_stmt.step()) {
          final Station _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpFavicon;
          if (_stmt.isNull(_columnIndexOfFavicon)) {
            _tmpFavicon = null;
          } else {
            _tmpFavicon = _stmt.getText(_columnIndexOfFavicon);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpCountry;
          if (_stmt.isNull(_columnIndexOfCountry)) {
            _tmpCountry = null;
          } else {
            _tmpCountry = _stmt.getText(_columnIndexOfCountry);
          }
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpCountrycode;
          if (_stmt.isNull(_columnIndexOfCountrycode)) {
            _tmpCountrycode = null;
          } else {
            _tmpCountrycode = _stmt.getText(_columnIndexOfCountrycode);
          }
          final String _tmpUrl_resolved;
          if (_stmt.isNull(_columnIndexOfUrlResolved)) {
            _tmpUrl_resolved = null;
          } else {
            _tmpUrl_resolved = _stmt.getText(_columnIndexOfUrlResolved);
          }
          final String _tmpState;
          if (_stmt.isNull(_columnIndexOfState)) {
            _tmpState = null;
          } else {
            _tmpState = _stmt.getText(_columnIndexOfState);
          }
          final String _tmpHomepage;
          if (_stmt.isNull(_columnIndexOfHomepage)) {
            _tmpHomepage = null;
          } else {
            _tmpHomepage = _stmt.getText(_columnIndexOfHomepage);
          }
          final int _tmpRank;
          _tmpRank = (int) (_stmt.getLong(_columnIndexOfRank));
          _item = new Station(_tmpId,_tmpFavicon,_tmpName,_tmpCountry,_tmpTags,_tmpCountrycode,_tmpUrl_resolved,_tmpState,_tmpHomepage,_tmpRank);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getStationByNameAndCountry(final String search, final String countryCode,
      final Continuation<? super List<Station>> $completion) {
    final String _sql = "SELECT * FROM radio_stations WHERE name LIKE '%' || ? || '%' COLLATE NOCASE and countrycode = (?)";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (search == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, search);
        }
        _argIndex = 2;
        if (countryCode == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, countryCode);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfFavicon = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "favicon");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfCountry = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "country");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfCountrycode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "countrycode");
        final int _columnIndexOfUrlResolved = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url_resolved");
        final int _columnIndexOfState = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "state");
        final int _columnIndexOfHomepage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "homepage");
        final int _columnIndexOfRank = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rank");
        final List<Station> _result = new ArrayList<Station>();
        while (_stmt.step()) {
          final Station _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpFavicon;
          if (_stmt.isNull(_columnIndexOfFavicon)) {
            _tmpFavicon = null;
          } else {
            _tmpFavicon = _stmt.getText(_columnIndexOfFavicon);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpCountry;
          if (_stmt.isNull(_columnIndexOfCountry)) {
            _tmpCountry = null;
          } else {
            _tmpCountry = _stmt.getText(_columnIndexOfCountry);
          }
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpCountrycode;
          if (_stmt.isNull(_columnIndexOfCountrycode)) {
            _tmpCountrycode = null;
          } else {
            _tmpCountrycode = _stmt.getText(_columnIndexOfCountrycode);
          }
          final String _tmpUrl_resolved;
          if (_stmt.isNull(_columnIndexOfUrlResolved)) {
            _tmpUrl_resolved = null;
          } else {
            _tmpUrl_resolved = _stmt.getText(_columnIndexOfUrlResolved);
          }
          final String _tmpState;
          if (_stmt.isNull(_columnIndexOfState)) {
            _tmpState = null;
          } else {
            _tmpState = _stmt.getText(_columnIndexOfState);
          }
          final String _tmpHomepage;
          if (_stmt.isNull(_columnIndexOfHomepage)) {
            _tmpHomepage = null;
          } else {
            _tmpHomepage = _stmt.getText(_columnIndexOfHomepage);
          }
          final int _tmpRank;
          _tmpRank = (int) (_stmt.getLong(_columnIndexOfRank));
          _item = new Station(_tmpId,_tmpFavicon,_tmpName,_tmpCountry,_tmpTags,_tmpCountrycode,_tmpUrl_resolved,_tmpState,_tmpHomepage,_tmpRank);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getFavoriteStations(
      final Continuation<? super Map<Station, Favorite>> $completion) {
    final String _sql = "SELECT * FROM radio_stations JOIN saved_items ON radio_stations.id = saved_items.id ORDER BY saved_items.`order`";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int[][] _statementIndices = AmbiguousColumnResolver.resolve(_stmt.getColumnNames(), new String[][] {{"id",
            "favicon", "name", "country", "tags", "countrycode", "url_resolved", "state",
            "homepage", "rank"}, {"id", "favId", "order"}});
        final Map<Station, Favorite> _result = new LinkedHashMap<Station, Favorite>();
        while (_stmt.step()) {
          final Station _key;
          final String _tmpId;
          if (_stmt.isNull(_statementIndices[0][0])) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_statementIndices[0][0]);
          }
          final String _tmpFavicon;
          if (_stmt.isNull(_statementIndices[0][1])) {
            _tmpFavicon = null;
          } else {
            _tmpFavicon = _stmt.getText(_statementIndices[0][1]);
          }
          final String _tmpName;
          if (_stmt.isNull(_statementIndices[0][2])) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_statementIndices[0][2]);
          }
          final String _tmpCountry;
          if (_stmt.isNull(_statementIndices[0][3])) {
            _tmpCountry = null;
          } else {
            _tmpCountry = _stmt.getText(_statementIndices[0][3]);
          }
          final String _tmpTags;
          if (_stmt.isNull(_statementIndices[0][4])) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_statementIndices[0][4]);
          }
          final String _tmpCountrycode;
          if (_stmt.isNull(_statementIndices[0][5])) {
            _tmpCountrycode = null;
          } else {
            _tmpCountrycode = _stmt.getText(_statementIndices[0][5]);
          }
          final String _tmpUrl_resolved;
          if (_stmt.isNull(_statementIndices[0][6])) {
            _tmpUrl_resolved = null;
          } else {
            _tmpUrl_resolved = _stmt.getText(_statementIndices[0][6]);
          }
          final String _tmpState;
          if (_stmt.isNull(_statementIndices[0][7])) {
            _tmpState = null;
          } else {
            _tmpState = _stmt.getText(_statementIndices[0][7]);
          }
          final String _tmpHomepage;
          if (_stmt.isNull(_statementIndices[0][8])) {
            _tmpHomepage = null;
          } else {
            _tmpHomepage = _stmt.getText(_statementIndices[0][8]);
          }
          final int _tmpRank;
          _tmpRank = (int) (_stmt.getLong(_statementIndices[0][9]));
          _key = new Station(_tmpId,_tmpFavicon,_tmpName,_tmpCountry,_tmpTags,_tmpCountrycode,_tmpUrl_resolved,_tmpState,_tmpHomepage,_tmpRank);
          if (_stmt.isNull(_statementIndices[1][0]) && _stmt.isNull(_statementIndices[1][1]) &&
              _stmt.isNull(_statementIndices[1][2])) {
            _result.put(_key, null);
            continue;
          }
          final Favorite _value;
          final String _tmpId_1;
          if (_stmt.isNull(_statementIndices[1][0])) {
            _tmpId_1 = null;
          } else {
            _tmpId_1 = _stmt.getText(_statementIndices[1][0]);
          }
          final Long _tmpFavId;
          if (_stmt.isNull(_statementIndices[1][1])) {
            _tmpFavId = null;
          } else {
            _tmpFavId = _stmt.getLong(_statementIndices[1][1]);
          }
          final Long _tmpOrder;
          if (_stmt.isNull(_statementIndices[1][2])) {
            _tmpOrder = null;
          } else {
            _tmpOrder = _stmt.getLong(_statementIndices[1][2]);
          }
          _value = new Favorite(_tmpFavId,_tmpId_1,_tmpOrder);
          if (!_result.containsKey(_key)) {
            _result.put(_key, _value);
          }
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getFavoriteStationById(final String id,
      final Continuation<? super Favorite> $completion) {
    final String _sql = "SELECT * FROM saved_items WHERE id = (?)";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfFavId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "favId");
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfOrder = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "order");
        final Favorite _result;
        if (_stmt.step()) {
          final Long _tmpFavId;
          if (_stmt.isNull(_columnIndexOfFavId)) {
            _tmpFavId = null;
          } else {
            _tmpFavId = _stmt.getLong(_columnIndexOfFavId);
          }
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final Long _tmpOrder;
          if (_stmt.isNull(_columnIndexOfOrder)) {
            _tmpOrder = null;
          } else {
            _tmpOrder = _stmt.getLong(_columnIndexOfOrder);
          }
          _result = new Favorite(_tmpFavId,_tmpId,_tmpOrder);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
