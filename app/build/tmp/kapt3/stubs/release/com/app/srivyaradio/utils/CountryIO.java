package com.app.srivyaradio.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\fJ&\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\n0\t2\b\b\u0002\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u0010\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\u0005H\u0002J\u000e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/app/srivyaradio/utils/CountryIO;", "", "<init>", "()V", "HEADER_NAME", "", "HEADER_CODE", "HEADER_ACTIVE", "readCsv", "", "Lcom/app/srivyaradio/data/models/CountryEntry;", "input", "Ljava/io/InputStream;", "writeCsv", "", "out", "Ljava/io/OutputStream;", "entries", "includeBom", "", "writeTemplateCsv", "toBool", "v", "sample", "app_release"})
public final class CountryIO {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String HEADER_NAME = "name";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String HEADER_CODE = "code";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String HEADER_ACTIVE = "active";
    @org.jetbrains.annotations.NotNull()
    public static final com.app.srivyaradio.utils.CountryIO INSTANCE = null;
    
    private CountryIO() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.app.srivyaradio.data.models.CountryEntry> readCsv(@org.jetbrains.annotations.NotNull()
    java.io.InputStream input) {
        return null;
    }
    
    public final void writeCsv(@org.jetbrains.annotations.NotNull()
    java.io.OutputStream out, @org.jetbrains.annotations.NotNull()
    java.util.List<com.app.srivyaradio.data.models.CountryEntry> entries, boolean includeBom) {
    }
    
    public final void writeTemplateCsv(@org.jetbrains.annotations.NotNull()
    java.io.OutputStream out) {
    }
    
    private final boolean toBool(java.lang.String v) {
        return false;
    }
    
    private final java.util.List<com.app.srivyaradio.data.models.CountryEntry> sample() {
        return null;
    }
}