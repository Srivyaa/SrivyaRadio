package com.app.srivyaradio.data.api.location;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005\u00c0\u0006\u0003"}, d2 = {"Lcom/app/srivyaradio/data/api/location/LocationInterface;", "", "getIpInfo", "Lcom/app/srivyaradio/data/models/Location;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface LocationInterface {
    
    @retrofit2.http.GET(value = "json")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getIpInfo(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.app.srivyaradio.data.models.Location> $completion);
}