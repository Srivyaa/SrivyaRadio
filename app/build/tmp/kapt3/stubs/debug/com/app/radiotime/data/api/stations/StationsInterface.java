package com.app.radiotime.data.api.stations;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\b\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\n\u00c0\u0006\u0003"}, d2 = {"Lcom/app/radiotime/data/api/stations/StationsInterface;", "", "getStations", "", "Lcom/app/radiotime/data/models/Station;", "country", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVersion", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface StationsInterface {
    
    @retrofit2.http.GET(value = "data/{country}.json")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStations(@retrofit2.http.Path(value = "country")
    @org.jetbrains.annotations.NotNull()
    java.lang.String country, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.app.radiotime.data.models.Station>> $completion);
    
    @retrofit2.http.GET(value = "version.txt")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVersion(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
}