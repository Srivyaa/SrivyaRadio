import SwiftUI
import CoreData

@main
struct SrivyaRadioApp: App {
    let persistenceController = CoreDataManager.shared.persistentContainer
    
    // Register App Delegate for Google Mobile Ads if handy or just use init
    // Google ads initialized in AdMobManager init called by usages.

    @StateObject private var mediaPlayerManager = MediaPlayerManager()
    @StateObject private var databaseRepository = CoreDataRepository()
    @StateObject private var purchaseManager = PurchaseManager()
    @StateObject private var themeManager = ThemeManager()
    @StateObject private var deepLinkManager = DeepLinkManager.shared
    
    init() {
        BackgroundTaskManager.shared.registerBackgroundTasks()
        if #available(iOS 16.0, *) {
            Task {
                try? await SrivyaRadioShortcuts.updateAppShortcuts()
            }
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.viewContext)
                .environmentObject(mediaPlayerManager)
                .environmentObject(databaseRepository)
                .environmentObject(purchaseManager)
                .environmentObject(themeManager)
                .environmentObject(deepLinkManager)
                .onAppear {
                    // Apply theme on app launch
                    themeManager.applyTheme()
                }
                .onOpenURL { url in
                    deepLinkManager.handle(url: url)
                }
        }
    }
}

// MARK: - Content View

struct ContentView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @EnvironmentObject var deepLinkManager: DeepLinkManager
    
    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $deepLinkManager.selectedTab) {
                // Discover Tab
                NavigationStack {
                    DiscoverScreen()
                }
                .tabItem {
                    Image(systemName: "radio")
                    Text("Discover")
                }
                .tag(0)
                
                // Favorites Tab
                NavigationStack {
                    FavoritesScreen()
                }
                .tabItem {
                    Image(systemName: "heart")
                    Text("Favorites")
                }
                .tag(1)
                
                // More Tab
                NavigationStack {
                    MoreScreen()
                }
                .tabItem {
                    Image(systemName: "ellipsis.circle")
                    Text("More")
                }
                .tag(2)
            }
            .accentColor(.primary)
            
            // Banner Ad
            if !PurchaseManager.isPremiumUser {
                BannerAdView()
                    .frame(height: 50)
            }
        }
    }
}