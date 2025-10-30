package com.app.srivyaradio.data.repositories;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u001c\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0013\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u0015\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ$\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u001a\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u001a0\u001dH\u0086@\u00a2\u0006\u0002\u0010\u001eJ\u0018\u0010\u001f\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u0013\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010 \u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u0016\u0010\"\u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u001c\u0010#\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010$\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/app/srivyaradio/data/repositories/DatabaseRepository;", "", "application", "Landroid/app/Application;", "<init>", "(Landroid/app/Application;)V", "entityDao", "Lcom/app/srivyaradio/data/database/EntityDao;", "getAllStations", "", "Lcom/app/srivyaradio/data/models/Station;", "countryCode", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertStations", "", "radioStations", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRadioStationByID", "id", "getRadioStationByName", "name", "getRadioStationByNameAndCountry", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertFavoriteItem", "favoriteStation", "Lcom/app/srivyaradio/data/models/Favorite;", "(Lcom/app/srivyaradio/data/models/Favorite;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFavoriteStations", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFavoriteItemById", "deleteFavoriteItem", "item", "updateFavoriteItem", "searchStations", "query", "app_release"})
public final class DatabaseRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.app.srivyaradio.data.database.EntityDao entityDao = null;
    
    public DatabaseRepository(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllStations(@org.jetbrains.annotations.NotNull()
    java.lang.String countryCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.srivyaradio.data.models.Station>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertStations(@org.jetbrains.annotations.NotNull()
    java.util.List<com.app.srivyaradio.data.models.Station> radioStations, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRadioStationByID(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.app.srivyaradio.data.models.Station> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRadioStationByName(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.srivyaradio.data.models.Station>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRadioStationByNameAndCountry(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String countryCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.srivyaradio.data.models.Station>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertFavoriteItem(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Favorite favoriteStation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFavoriteStations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.Map<com.app.srivyaradio.data.models.Station, com.app.srivyaradio.data.models.Favorite>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFavoriteItemById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.app.srivyaradio.data.models.Favorite> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteFavoriteItem(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Favorite item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateFavoriteItem(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Favorite item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Search for stations by name (case-insensitive)
     * @param query The search query string
     * @return List of matching stations
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchStations(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.srivyaradio.data.models.Station>> $completion) {
        return null;
    }
}