package com.app.srivyaradio.data.repositories;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007J\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0007J\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u0007J\b\u0010\u000f\u001a\u0004\u0018\u00010\u0007J\u000e\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012J\u0006\u0010\u0013\u001a\u00020\u0012J\u000e\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u0012J\u0006\u0010\u0016\u001a\u00020\u0012J\u000e\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\tJ\u0006\u0010\u0019\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/app/srivyaradio/data/repositories/SharedPreferencesRepository;", "", "sharedPreferences", "Landroid/content/SharedPreferences;", "<init>", "(Landroid/content/SharedPreferences;)V", "getCountryCode", "", "isFirstStartUp", "", "setUserCountry", "", "code", "setLastPlayID", "stationId", "getLastPlayID", "setThemeMode", "mode", "", "getThemeMode", "setDBVersion", "version", "getDBVersion", "setHasPurchased", "hasPurchased", "getHasPurchased", "app_debug"})
public final class SharedPreferencesRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences sharedPreferences = null;
    
    public SharedPreferencesRepository(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences sharedPreferences) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCountryCode() {
        return null;
    }
    
    public final boolean isFirstStartUp() {
        return false;
    }
    
    public final void setUserCountry(@org.jetbrains.annotations.NotNull()
    java.lang.String code) {
    }
    
    public final void setLastPlayID(@org.jetbrains.annotations.NotNull()
    java.lang.String stationId) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLastPlayID() {
        return null;
    }
    
    public final void setThemeMode(int mode) {
    }
    
    public final int getThemeMode() {
        return 0;
    }
    
    public final void setDBVersion(int version) {
    }
    
    public final int getDBVersion() {
        return 0;
    }
    
    public final void setHasPurchased(boolean hasPurchased) {
    }
    
    public final boolean getHasPurchased() {
        return false;
    }
}