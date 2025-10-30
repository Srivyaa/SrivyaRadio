package com.app.srivyaradio.media;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u00018B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010&\u001a\u00020\'H\u0002J\b\u0010(\u001a\u00020\'H\u0002J\b\u0010)\u001a\u00020\'H\u0003J\b\u0010*\u001a\u00020\'H\u0017J\u0010\u0010+\u001a\u00020\u000b2\u0006\u0010,\u001a\u00020-H\u0016J\u0018\u0010.\u001a\u00020\'2\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u000202H\u0016J\u0012\u00103\u001a\u00020\'2\b\u00104\u001a\u0004\u0018\u000105H\u0016J\b\u00106\u001a\u00020\'H\u0016J\b\u00107\u001a\u00020\'H\u0002R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010 \u001a\u00020!X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%\u00a8\u00069"}, d2 = {"Lcom/app/srivyaradio/media/PlayerService;", "Landroidx/media3/session/MediaLibraryService;", "<init>", "()V", "player", "Landroidx/media3/exoplayer/ExoPlayer;", "getPlayer", "()Landroidx/media3/exoplayer/ExoPlayer;", "setPlayer", "(Landroidx/media3/exoplayer/ExoPlayer;)V", "mediaLibrarySession", "Landroidx/media3/session/MediaLibraryService$MediaLibrarySession;", "dbRepository", "Lcom/app/srivyaradio/data/repositories/DatabaseRepository;", "repository", "Lcom/app/srivyaradio/data/repositories/SharedPreferencesRepository;", "serviceJob", "Lkotlinx/coroutines/CompletableJob;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "countryCode", "", "timer", "Landroid/os/CountDownTimer;", "currentQueue", "", "Landroidx/media3/common/MediaItem;", "currentQueueIndex", "", "currentQueueTag", "retrofit", "Lretrofit2/Retrofit;", "apiInterface", "Lcom/app/srivyaradio/data/api/location/LocationInterface;", "getApiInterface", "()Lcom/app/srivyaradio/data/api/location/LocationInterface;", "setApiInterface", "(Lcom/app/srivyaradio/data/api/location/LocationInterface;)V", "updatePlaybackState", "", "updateCustomActions", "updateAvailableCommands", "onCreate", "onGetSession", "controllerInfo", "Landroidx/media3/session/MediaSession$ControllerInfo;", "onUpdateNotification", "session", "Landroidx/media3/session/MediaSession;", "startInForegroundRequired", "", "onTaskRemoved", "rootIntent", "Landroid/content/Intent;", "onDestroy", "release", "MediaLibrarySessionCallback", "app_release"})
public final class PlayerService extends androidx.media3.session.MediaLibraryService {
    public androidx.media3.exoplayer.ExoPlayer player;
    private androidx.media3.session.MediaLibraryService.MediaLibrarySession mediaLibrarySession;
    private com.app.srivyaradio.data.repositories.DatabaseRepository dbRepository;
    private com.app.srivyaradio.data.repositories.SharedPreferencesRepository repository;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CompletableJob serviceJob = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String countryCode = "US";
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer timer;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<androidx.media3.common.MediaItem> currentQueue = null;
    private int currentQueueIndex = -1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentQueueTag = "discover";
    @org.jetbrains.annotations.NotNull()
    private final retrofit2.Retrofit retrofit = null;
    @org.jetbrains.annotations.NotNull()
    private com.app.srivyaradio.data.api.location.LocationInterface apiInterface;
    
    public PlayerService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.media3.exoplayer.ExoPlayer getPlayer() {
        return null;
    }
    
    public final void setPlayer(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.ExoPlayer p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.app.srivyaradio.data.api.location.LocationInterface getApiInterface() {
        return null;
    }
    
    public final void setApiInterface(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.api.location.LocationInterface p0) {
    }
    
    private final void updatePlaybackState() {
    }
    
    private final void updateCustomActions() {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    private final void updateAvailableCommands() {
    }
    
    @java.lang.Override()
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    public void onCreate() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.media3.session.MediaLibraryService.MediaLibrarySession onGetSession(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controllerInfo) {
        return null;
    }
    
    @java.lang.Override()
    public void onUpdateNotification(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession session, boolean startInForegroundRequired) {
    }
    
    @java.lang.Override()
    public void onTaskRemoved(@org.jetbrains.annotations.Nullable()
    android.content.Intent rootIntent) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    private final void release() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0082\u0004\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J \u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0016J\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0017J.\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0016J.\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u000e2\u0006\u0010\u0006\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\t2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0017J,\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u000e2\u0006\u0010\u0006\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\t2\u0006\u0010\u001c\u001a\u00020\u001dH\u0016JL\u0010\u001e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u001f0\u00150\u000e2\u0006\u0010\u0006\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\t2\u0006\u0010 \u001a\u00020\u001d2\u0006\u0010!\u001a\u00020\u00052\u0006\u0010\"\u001a\u00020\u00052\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0016J6\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020$0\u00150\u000e2\u0006\u0010\u0006\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\t2\u0006\u0010 \u001a\u00020\u001d2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0016J<\u0010%\u001a\b\u0012\u0004\u0012\u00020&0\u000e2\u0006\u0010\'\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00160)2\u0006\u0010*\u001a\u00020\u00052\u0006\u0010+\u001a\u00020,H\u0017\u00a8\u0006-"}, d2 = {"Lcom/app/srivyaradio/media/PlayerService$MediaLibrarySessionCallback;", "Landroidx/media3/session/MediaLibraryService$MediaLibrarySession$Callback;", "<init>", "(Lcom/app/srivyaradio/media/PlayerService;)V", "onPlayerCommandRequest", "", "session", "Landroidx/media3/session/MediaSession;", "controller", "Landroidx/media3/session/MediaSession$ControllerInfo;", "playerCommand", "onConnect", "Landroidx/media3/session/MediaSession$ConnectionResult;", "onCustomCommand", "Lcom/google/common/util/concurrent/ListenableFuture;", "Landroidx/media3/session/SessionResult;", "customCommand", "Landroidx/media3/session/SessionCommand;", "args", "Landroid/os/Bundle;", "onGetLibraryRoot", "Landroidx/media3/session/LibraryResult;", "Landroidx/media3/common/MediaItem;", "Landroidx/media3/session/MediaLibraryService$MediaLibrarySession;", "browser", "params", "Landroidx/media3/session/MediaLibraryService$LibraryParams;", "onGetItem", "mediaId", "", "onGetChildren", "Lcom/google/common/collect/ImmutableList;", "parentId", "page", "pageSize", "onSubscribe", "Ljava/lang/Void;", "onSetMediaItems", "Landroidx/media3/session/MediaSession$MediaItemsWithStartPosition;", "mediaSession", "mediaItems", "", "startIndex", "startPositionMs", "", "app_release"})
    final class MediaLibrarySessionCallback implements androidx.media3.session.MediaLibraryService.MediaLibrarySession.Callback {
        
        public MediaLibrarySessionCallback() {
            super();
        }
        
        @java.lang.Override()
        public int onPlayerCommandRequest(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo controller, int playerCommand) {
            return 0;
        }
        
        @java.lang.Override()
        @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
        @org.jetbrains.annotations.NotNull()
        public androidx.media3.session.MediaSession.ConnectionResult onConnect(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo controller) {
            return null;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> onCustomCommand(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo controller, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.SessionCommand customCommand, @org.jetbrains.annotations.NotNull()
        android.os.Bundle args) {
            return null;
        }
        
        @java.lang.Override()
        @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.LibraryResult<androidx.media3.common.MediaItem>> onGetLibraryRoot(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaLibraryService.MediaLibrarySession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo browser, @org.jetbrains.annotations.Nullable()
        androidx.media3.session.MediaLibraryService.LibraryParams params) {
            return null;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.LibraryResult<androidx.media3.common.MediaItem>> onGetItem(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaLibraryService.MediaLibrarySession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo browser, @org.jetbrains.annotations.NotNull()
        java.lang.String mediaId) {
            return null;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.LibraryResult<com.google.common.collect.ImmutableList<androidx.media3.common.MediaItem>>> onGetChildren(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaLibraryService.MediaLibrarySession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo browser, @org.jetbrains.annotations.NotNull()
        java.lang.String parentId, int page, int pageSize, @org.jetbrains.annotations.Nullable()
        androidx.media3.session.MediaLibraryService.LibraryParams params) {
            return null;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.LibraryResult<java.lang.Void>> onSubscribe(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaLibraryService.MediaLibrarySession session, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo browser, @org.jetbrains.annotations.NotNull()
        java.lang.String parentId, @org.jetbrains.annotations.Nullable()
        androidx.media3.session.MediaLibraryService.LibraryParams params) {
            return null;
        }
        
        @java.lang.Override()
        @androidx.media3.common.util.UnstableApi()
        @org.jetbrains.annotations.NotNull()
        public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.MediaSession.MediaItemsWithStartPosition> onSetMediaItems(@org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession mediaSession, @org.jetbrains.annotations.NotNull()
        androidx.media3.session.MediaSession.ControllerInfo controller, @org.jetbrains.annotations.NotNull()
        java.util.List<androidx.media3.common.MediaItem> mediaItems, int startIndex, long startPositionMs) {
            return null;
        }
    }
}