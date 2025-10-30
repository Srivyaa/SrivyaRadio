package com.app.srivyaradio.media;

/**
 * Manages the queue of stations for the media player
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\r\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0018\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\b2\b\b\u0002\u0010\u001a\u001a\u00020\u001bJ\u000e\u0010\u001c\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\bJ\u0006\u0010\u001d\u001a\u00020\u0018J$\u0010\u001e\u001a\u00020\u00182\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\b0\u000e2\u0006\u0010 \u001a\u00020\b2\u0006\u0010!\u001a\u00020\fJ\b\u0010\"\u001a\u00020\u0018H\u0002J\b\u0010#\u001a\u0004\u0018\u00010\bJ\b\u0010$\u001a\u0004\u0018\u00010\bJ\u0006\u0010\u001a\u001a\u00020\u0018J\u0006\u0010%\u001a\u00020\u0018J\u0006\u0010&\u001a\u00020\u001bJ\u0006\u0010\'\u001a\u00020\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\b0\u000e8F\u00a2\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0011\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006("}, d2 = {"Lcom/app/srivyaradio/media/QueueManager;", "", "viewModel", "Lcom/app/srivyaradio/ui/MainViewModel;", "<init>", "(Lcom/app/srivyaradio/ui/MainViewModel;)V", "_currentQueue", "Landroidx/compose/runtime/snapshots/SnapshotStateList;", "Lcom/app/srivyaradio/data/models/Station;", "_currentIndex", "", "_currentTag", "", "currentQueue", "", "getCurrentQueue", "()Ljava/util/List;", "currentIndex", "getCurrentIndex", "()I", "currentTag", "getCurrentTag", "()Ljava/lang/String;", "addToQueue", "", "station", "playNext", "", "removeFromQueue", "clearQueue", "setQueue", "stations", "currentStation", "tag", "syncQueueWithService", "getNextStation", "getPreviousStation", "playPrevious", "hasNext", "hasPrevious", "app_release"})
public final class QueueManager {
    @org.jetbrains.annotations.NotNull()
    private final com.app.srivyaradio.ui.MainViewModel viewModel = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.snapshots.SnapshotStateList<com.app.srivyaradio.data.models.Station> _currentQueue = null;
    private int _currentIndex = -1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String _currentTag = "discover";
    
    public QueueManager(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.ui.MainViewModel viewModel) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.app.srivyaradio.data.models.Station> getCurrentQueue() {
        return null;
    }
    
    public final int getCurrentIndex() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentTag() {
        return null;
    }
    
    /**
     * Add a station to the queue
     */
    public final void addToQueue(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Station station, boolean playNext) {
    }
    
    /**
     * Remove a station from the queue
     */
    public final void removeFromQueue(@org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Station station) {
    }
    
    /**
     * Clear the queue
     */
    public final void clearQueue() {
    }
    
    /**
     * Set the entire queue with a list of stations and the current playing station
     */
    public final void setQueue(@org.jetbrains.annotations.NotNull()
    java.util.List<com.app.srivyaradio.data.models.Station> stations, @org.jetbrains.annotations.NotNull()
    com.app.srivyaradio.data.models.Station currentStation, @org.jetbrains.annotations.NotNull()
    java.lang.String tag) {
    }
    
    /**
     * Sync the current queue with the PlayerService
     */
    private final void syncQueueWithService() {
    }
    
    /**
     * Get the next station in the queue
     */
    @org.jetbrains.annotations.Nullable()
    public final com.app.srivyaradio.data.models.Station getNextStation() {
        return null;
    }
    
    /**
     * Get the previous station in the queue
     */
    @org.jetbrains.annotations.Nullable()
    public final com.app.srivyaradio.data.models.Station getPreviousStation() {
        return null;
    }
    
    /**
     * Play the next station in the queue
     */
    public final void playNext() {
    }
    
    /**
     * Play the previous station in the queue
     */
    public final void playPrevious() {
    }
    
    /**
     * Check if there is a next station in the queue
     */
    public final boolean hasNext() {
        return false;
    }
    
    /**
     * Check if there is a previous station in the queue
     */
    public final boolean hasPrevious() {
        return false;
    }
}