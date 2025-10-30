package com.app.srivyaradio.data.database;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0007"}, d2 = {"Lcom/app/srivyaradio/data/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "<init>", "()V", "radioStationDao", "Lcom/app/srivyaradio/data/database/EntityDao;", "Companion", "app_release"})
@androidx.room.Database(entities = {com.app.srivyaradio.data.models.Station.class, com.app.srivyaradio.data.models.Favorite.class}, version = 1)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.app.srivyaradio.data.database.AppDatabase instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.app.srivyaradio.data.database.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.app.srivyaradio.data.database.EntityDao radioStationDao();
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bJ\u0010\u0010\t\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/app/srivyaradio/data/database/AppDatabase$Companion;", "", "<init>", "()V", "instance", "Lcom/app/srivyaradio/data/database/AppDatabase;", "getInstance", "context", "Landroid/content/Context;", "buildDatabase", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.app.srivyaradio.data.database.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        private final com.app.srivyaradio.data.database.AppDatabase buildDatabase(android.content.Context context) {
            return null;
        }
    }
}