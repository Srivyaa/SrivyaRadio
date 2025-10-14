package com.app.radiotime.data.database;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010$\n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0018\u0010\b\u001a\u0004\u0018\u00010\u00042\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001c\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u000b\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u001c\u0010\u0016\u001a\u00020\u000f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00140\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u001a\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00140\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u0018\u0010\u001b\u001a\u0004\u0018\u00010\u00142\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u0016\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u001c\u0010 \u001a\u00020\u000f2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00140\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0011\u00a8\u0006\"\u00c0\u0006\u0003"}, d2 = {"Lcom/app/radiotime/data/database/EntityDao;", "", "getStations", "", "Lcom/app/radiotime/data/models/Station;", "countryCode", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getStationById", "id", "getStationByName", "search", "getStationByNameAndCountry", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertStation", "", "radioStations", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertFavoriteItem", "favoriteStation", "Lcom/app/radiotime/data/models/Favorite;", "(Lcom/app/radiotime/data/models/Favorite;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertFavoriteItems", "favoriteStations", "getFavoriteStations", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFavoriteStationById", "updateFavoriteItem", "item", "deleteFavoriteStation", "favItem", "deleteFavoriteStations", "favItems", "app_debug"})
@androidx.room.Dao()
public abstract interface EntityDao {
    
    @androidx.room.Query(value = "SELECT * FROM radio_stations WHERE countrycode = (:countryCode) ORDER BY rank ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStations(@org.jetbrains.annotations.NotNull()
    java.lang.String countryCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.radiotime.data.models.Station>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM radio_stations WHERE id = (:id)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStationById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.app.radiotime.data.models.Station> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM radio_stations WHERE name LIKE \'%\' || :search || \'%\' COLLATE NOCASE")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStationByName(@org.jetbrains.annotations.NotNull()
    java.lang.String search, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.radiotime.data.models.Station>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM radio_stations WHERE name LIKE \'%\' || :search || \'%\' COLLATE NOCASE and countrycode = (:countryCode)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStationByNameAndCountry(@org.jetbrains.annotations.NotNull()
    java.lang.String search, @org.jetbrains.annotations.NotNull()
    java.lang.String countryCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.radiotime.data.models.Station>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertStation(@org.jetbrains.annotations.NotNull()
    java.util.List<com.app.radiotime.data.models.Station> radioStations, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFavoriteItem(@org.jetbrains.annotations.NotNull()
    com.app.radiotime.data.models.Favorite favoriteStation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFavoriteItems(@org.jetbrains.annotations.NotNull()
    java.util.List<com.app.radiotime.data.models.Favorite> favoriteStations, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM radio_stations JOIN saved_items ON radio_stations.id = saved_items.id ORDER BY saved_items.`order`")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFavoriteStations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.Map<com.app.radiotime.data.models.Station, com.app.radiotime.data.models.Favorite>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM saved_items WHERE id = (:id)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFavoriteStationById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.app.radiotime.data.models.Favorite> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFavoriteItem(@org.jetbrains.annotations.NotNull()
    com.app.radiotime.data.models.Favorite item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFavoriteStation(@org.jetbrains.annotations.NotNull()
    com.app.radiotime.data.models.Favorite favItem, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFavoriteStations(@org.jetbrains.annotations.NotNull()
    java.util.List<com.app.radiotime.data.models.Favorite> favItems, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}