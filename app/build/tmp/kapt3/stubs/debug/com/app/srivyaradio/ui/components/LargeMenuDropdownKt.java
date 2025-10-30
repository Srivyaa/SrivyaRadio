package com.app.srivyaradio.ui.components;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a\u00cf\u0001\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u00022\b\b\u0002\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u0002H\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r26\u0010\u000e\u001a2\u0012\u0013\u0012\u00110\r\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u0011H\u0002\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\u0004\u0012\u00020\u00010\u000f2\u0014\b\u0002\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u00020\b0\u001521\b\u0002\u0010\u0016\u001a+\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0006\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00010\u0018\u0012\u0004\u0012\u00020\u00010\u0017\u00a2\u0006\u0002\b\u0019H\u0007\u001a.\u0010\u001a\u001a\u00020\u00012\u0006\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00010\u0018H\u0007\u00a8\u0006\u001e"}, d2 = {"LargeDropdownMenu", "", "T", "modifier", "Landroidx/compose/ui/Modifier;", "enabled", "", "label", "", "notSetLabel", "items", "", "selectedIndex", "", "onItemSelected", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "index", "item", "selectedItemToString", "Lkotlin/Function1;", "drawItem", "Lkotlin/Function4;", "Lkotlin/Function0;", "Landroidx/compose/runtime/Composable;", "LargeDropdownMenuItem", "text", "selected", "onClick", "app_debug"})
public final class LargeMenuDropdownKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final <T extends java.lang.Object>void LargeDropdownMenu(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.Nullable()
    java.lang.String notSetLabel, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> items, int selectedIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super T, kotlin.Unit> onItemSelected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, java.lang.String> selectedItemToString, @org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.internal.ComposableFunction4<? super T, ? super java.lang.Boolean, ? super java.lang.Boolean, ? super kotlin.jvm.functions.Function0<kotlin.Unit>, kotlin.Unit> drawItem) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void LargeDropdownMenuItem(@org.jetbrains.annotations.NotNull()
    java.lang.String text, boolean selected, boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}