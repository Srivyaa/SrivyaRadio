# SrivyaRadio iOS Porting Plan

## Executive Summary

This comprehensive plan outlines the migration of the SrivyaRadio Android application to iOS, including all major features and functionality. The app is a sophisticated radio streaming platform with offline downloads, favorites management, country-based browsing, Android Auto integration, and premium features.

## Current Android App Analysis

### Core Features
- **Radio Streaming**: Live radio stations with search, favorites, and offline downloads
- **Country-Based Browsing**: Dynamic country selection with user-customizable lists
- **Offline Playback**: Downloaded audio files for offline listening
- **Media Session Integration**: Background playback with Android Auto support
- **Premium Features**: In-app purchases via RevenueCat
- **Ad Integration**: Google Mobile Ads for monetization
- **Advanced Controls**: Sleep timer, shuffle, repeat, seek functionality

### Technical Architecture
- **Framework**: Android (Kotlin) with Jetpack Compose
- **Database**: Room with SQLite
- **Media**: ExoPlayer/Media3 with MediaSession
- **Networking**: Retrofit with OkHttp
- **Architecture**: MVVM with Repository pattern
- **Background Tasks**: WorkManager for downloads

## iOS Porting Strategy

### 1. Project Setup & Architecture Analysis

**Objective**: Establish the iOS project foundation and understand complete app architecture.

**Implementation Steps**:
1. Create new Xcode project with SwiftUI interface
2. Configure project settings for iOS 15+ deployment target
3. Set up code signing and provisioning profiles
4. Analyze all Android components and dependencies

**Key Deliverables**:
- iOS project with proper configuration
- Architecture documentation mapping Android to iOS components
- Dependency management setup (CocoaPods/Swift Package Manager)

**Technical Requirements**:
```bash
# Required iOS Capabilities
- Background Modes: Audio, Background Processing
- App Transport Security exceptions for HTTP APIs
- User Permissions: Microphone (if needed), Background App Refresh
```

### 2. Data Layer Migration - Core Data Setup

**Objective**: Replace Room database with Core Data while maintaining data consistency.

**Implementation Details**:
```swift
// Core Data Model Objects
@NSManaged public var id: String
@NSManaged public var favicon: String
@NSManaged public var name: String
@NSManaged public var country: String
@NSManaged public var tags: String
@NSManaged public var countrycode: String
@NSManaged public var url_resolved: String
@NSManaged public var state: String
@NSManaged public var homepage: String
@NSManaged public var rank: Int32

// Core Data Stack
class CoreDataManager {
    static let shared = CoreDataManager()
    let persistentContainer: NSPersistentContainer
    
    private init() {
        persistentContainer = NSPersistentContainer(name: "SrivyaRadio")
        persistentContainer.loadPersistentStores { _, error in
            if let error = error {
                fatalError("Core Data store failed to load: \(error)")
            }
        }
    }
}
```

**Migration Tasks**:
- Create Core Data model with Station, Favorite, DownloadedItem entities
- Implement Core Data stack with NSPersistentContainer
- Port Room queries to NSPredicate and NSFetchRequest
- Add Core Data lightweight migration support

### 3. Data Models & Networking Layer

**Objective**: Convert Kotlin data classes to Swift structs and implement modern networking.

**Implementation Details**:
```swift
// Swift Data Models
struct Station: Codable, Identifiable {
    let id: String
    var favicon: String
    var name: String
    var country: String
    var tags: String
    var countrycode: String
    var url_resolved: String
    var state: String
    var homepage: String
    var rank: Int
    
    enum CodingKeys: String, CodingKey {
        case id
        case favicon
        case name
        case country
        case tags
        case countrycode
        case url_resolved
        case state
        case homepage
        case rank
    }
}

// Networking with URLSession
class StationsClient {
    static let shared = StationsClient()
    private let session = URLSession.shared
    
    func getStations(for country: String) async throws -> [Station] {
        let url = URL(string: "https://srivyaa.github.io/RadioStations/data/\(country).json")!
        let (data, _) = try await session.data(from: url)
        return try JSONDecoder().decode([Station].self, from: data)
    }
}
```

**Key Tasks**:
- Port all Kotlin data classes to Swift structs/classes
- Implement Codable protocols for JSON parsing
- Create modern async/await networking layer
- Add proper error handling and retry logic

### 4. Repository Pattern Implementation

**Objective**: Implement repository pattern for data management with async/await.

**Implementation Details**:
```swift
// Repository Protocol
protocol DatabaseRepository {
    func getStations(for country: String) async -> [Station]
    func searchStations(query: String) async -> [Station]
    func getFavorites() async -> [Station]
    func addToFavorites(station: Station) async
    func removeFromFavorites(station: Station) async
}

// Repository Implementation
class CoreDataRepository: DatabaseRepository {
    func getStations(for country: String) async -> [Station] {
        await withCheckedContinuation { continuation in
            let context = CoreDataManager.shared.persistentContainer.viewContext
            let request = StationEntity.fetchRequest()
            request.predicate = NSPredicate(format: "countrycode == %@", country)
            
            do {
                let results = try context.fetch(request)
                let stations = results.map { $0.toStation() }
                continuation.resume(returning: stations)
            } catch {
                continuation.resume(returning: [])
            }
        }
    }
}
```

### 5. Media Player Implementation

**Objective**: Replace ExoPlayer with AVPlayer and implement background playback.

**Implementation Details**:
```swift
// Media Player Manager
class MediaPlayerManager: ObservableObject {
    @Published var isPlaying = false
    @Published var currentStation: Station?
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    
    private let player = AVPlayer()
    private let audioSession = AVAudioSession.sharedInstance()
    
    func playStation(_ station: Station) {
        guard let url = URL(string: station.url_resolved) else { return }
        let playerItem = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: playerItem)
        player.play()
        isPlaying = true
        currentStation = station
    }
    
    func playPause() {
        if isPlaying {
            player.pause()
        } else {
            player.play()
        }
        isPlaying.toggle()
    }
}

// Background Audio Support
class AudioPlayerController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        setupAudioSession()
        setupRemoteCommandCenter()
    }
    
    private func setupAudioSession() {
        do {
            try audioSession.setCategory(.playback, mode: .default, options: [])
            try audioSession.setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }
    
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.player.play()
            return .success
        }
        
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.player.pause()
            return .success
        }
        
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.skipToNext()
            return .success
        }
        
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.skipToPrevious()
            return .success
        }
    }
}
```

### 6. SwiftUI Navigation & Routing

**Objective**: Implement modern SwiftUI navigation with deep linking support.

**Implementation Details**:
```swift
// Main App Structure
@main
struct SrivyaRadioApp: App {
    @StateObject private var playerManager = MediaPlayerManager()
    @StateObject private var databaseRepository = CoreDataRepository()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(playerManager)
                .environmentObject(databaseRepository)
        }
    }
}

// Navigation Structure
struct ContentView: View {
    var body: some View {
        TabView {
            NavigationStack {
                DiscoverScreen()
            }
            .tabItem {
                Image(systemName: "radio")
                Text("Discover")
            }
            
            NavigationStack {
                FavoritesScreen()
            }
            .tabItem {
                Image(systemName: "heart")
                Text("Favorites")
            }
            
            NavigationStack {
                MoreScreen()
            }
            .tabItem {
                Image(systemName: "ellipsis.circle")
                Text("More")
            }
        }
    }
}
```

### 7. UI Screens - Core Implementation

**Objective**: Port main screens with full functionality and native iOS design patterns.

**Discover Screen Implementation**:
```swift
struct DiscoverScreen: View {
    @EnvironmentObject var playerManager: MediaPlayerManager
    @EnvironmentObject var databaseRepository: DatabaseRepository
    @StateObject private var viewModel = DiscoverViewModel()
    
    var body: some View {
        List {
            // Country Selection
            Picker("Country", selection: $viewModel.selectedCountry) {
                ForEach(viewModel.countries, id: \.self) { country in
                    Text(country.name).tag(country.code)
                }
            }
            .pickerStyle(.menu)
            .onChange(of: viewModel.selectedCountry) { newCountry in
                Task {
                    await viewModel.loadStations(for: newCountry)
                }
            }
            
            // Stations List
            ForEach(viewModel.stations) { station in
                StationRow(station: station)
                    .onTapGesture {
                        playerManager.playStation(station)
                    }
            }
        }
        .refreshable {
            await viewModel.refreshStations()
        }
        .searchable(text: $viewModel.searchText)
        .onChange(of: viewModel.searchText) { searchText in
            Task {
                await viewModel.searchStations(query: searchText)
            }
        }
    }
}

struct StationRow: View {
    let station: Station
    
    var body: some View {
        HStack {
            AsyncImage(url: URL(string: station.favicon)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Image(systemName: "radio")
                    .foregroundColor(.gray)
            }
            .frame(width: 50, height: 50)
            .cornerRadius(8)
            
            VStack(alignment: .leading) {
                Text(station.name)
                    .font(.headline)
                Text(station.country)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Button(action: {
                // Add to favorites
            }) {
                Image(systemName: "heart")
            }
        }
    }
}
```

### 8. UI Screens - Secondary Features

**Objective**: Implement remaining screens with proper functionality.

**Favorites Screen with Drag & Drop**:
```swift
struct FavoritesScreen: View {
    @EnvironmentObject var playerManager: MediaPlayerManager
    @StateObject private var viewModel = FavoritesViewModel()
    @State private var editMode = EditMode.inactive
    
    var body: some View {
        List {
            ForEach(viewModel.favorites) { station in
                StationRow(station: station)
                    .onTapGesture {
                        playerManager.playStation(station)
                    }
                    .moveDisabled(editMode == .inactive)
            }
            .onDelete(perform: viewModel.deleteFavorites)
            .onMove(perform: viewModel.moveFavorites)
        }
        .environment(\.editMode, $editMode)
        .navigationTitle("Favorites")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(editMode == .active ? "Done" : "Edit") {
                    editMode.toggle()
                }
            }
        }
    }
}
```

### 9. Advanced Media Features

**Objective**: Implement sophisticated playback controls and features.

**Sleep Timer Implementation**:
```swift
class SleepTimerManager: ObservableObject {
    @Published var timeRemaining: TimeInterval = 0
    @Published var isActive = false
    
    private var timer: Timer?
    private let playerManager: MediaPlayerManager
    
    init(playerManager: MediaPlayerManager) {
        self.playerManager = playerManager
    }
    
    func startTimer(minutes: Int) {
        stopTimer()
        timeRemaining = Double(minutes * 60)
        isActive = true
        
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.timeRemaining -= 1
            
            if self?.timeRemaining ?? 0 <= 0 {
                self?.stopTimer()
                self?.playerManager.pause()
            }
        }
    }
    
    func stopTimer() {
        timer?.invalidate()
        timer = nil
        isActive = false
        timeRemaining = 0
    }
}
```

### 10. Background Processing & Downloads

**Objective**: Implement background download management with iOS BackgroundTasks.

**Implementation Details**:
```swift
// Download Manager
class DownloadManager: ObservableObject {
    @Published var downloads: [DownloadTask] = []
    
    func downloadStation(_ station: Station) {
        guard let url = URL(string: station.url_resolved) else { return }
        
        let downloadTask = URLSession.shared.downloadTask(with: url) { [weak self] url, response, error in
            guard let self = self,
                  let localURL = url,
                  error == nil else { return }
            
            // Save to documents directory
            let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let destinationURL = documentsPath.appendingPathComponent("\(station.name).mp3")
            
            do {
                if FileManager.default.fileExists(atPath: destinationURL.path) {
                    try FileManager.default.removeItem(at: destinationURL)
                }
                try FileManager.default.moveItem(at: localURL, to: destinationURL)
                
                // Save to Core Data
                self.saveDownloadedItem(station: station, fileURL: destinationURL)
                
                DispatchQueue.main.async {
                    self.downloads.removeAll { $0.station.id == station.id }
                }
            } catch {
                print("Download failed: \(error)")
            }
        }
        
        downloadTask.resume()
        downloads.append(DownloadTask(station: station, task: downloadTask))
    }
}

// Background App Refresh
class BackgroundTaskManager {
    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.srivyaradio.refresh", using: nil) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }
    }
    
    func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: "com.srivyaradio.refresh")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60) // 15 minutes
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Failed to schedule app refresh: \(error)")
        }
    }
    
    private func handleAppRefresh(task: BGAppRefreshTask) {
        // Perform refresh operations
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        // Complete the task
        task.setTaskCompleted(success: true)
    }
}
```

### 11. CarPlay Integration

**Objective**: Implement CarPlay framework for in-car experience.

**Implementation Details**:
```swift
// CarPlay Controller
class CarPlayController: CPTemplateApplicationSceneDelegate {
    func templateApplicationScene(_ templateApplicationScene: CPTemplateApplicationScene, didConnect interfaceController: CPInterfaceController) {
        // Create CarPlay interface
        let listTemplate = CPListTemplate(title: "SrivyaRadio", sections: [])
        interfaceController.setRootTemplate(listTemplate, animated: true)
        
        // Add now playing template
        let nowPlaying = CPNowPlayingTemplate.shared()
        nowPlaying.isEligibleForCarPlay = true
        nowPlaying.upNextTitle = "Up Next"
        
        // Add tab bar template if needed
        let tabbedTemplate = CPTabBarTemplate(templates: [listTemplate])
        interfaceController.setRootTemplate(tabbedTemplate, animated: true)
    }
    
    func templateApplicationScene(_ templateApplicationScene: CPTemplateApplicationScene, didDisconnect interfaceController: CPInterfaceController) {
        // Clean up CarPlay connection
    }
}
```

### 12. Third-Party Integrations

**Objective**: Integrate Google Ads and RevenueCat for monetization.

**Google Ads Integration**:
```swift
import GoogleMobileAds

class AdManager: NSObject, GADFullScreenContentDelegate {
    private var interstitial: GADInterstitialAd?
    
    func loadInterstitialAd() {
        let request = GADRequest()
        GADInterstitialAd.load(withAdUnitID: "ca-app-pub-3940256099942544/1033173712", request: request) { [weak self] ad, error in
            if let error = error {
                print("Failed to load interstitial ad: \(error)")
                return
            }
            
            self?.interstitial = ad
            self?.interstitial?.fullScreenContentDelegate = self
        }
    }
    
    func showInterstitialAd() {
        guard let interstitial = interstitial else {
            print("Ad wasn't ready")
            return
        }
        
        interstitial.present(fromRootViewController: UIApplication.shared.windows.first?.rootViewController)
    }
    
    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        print("Ad dismissed")
        loadInterstitialAd() // Load next ad
    }
}
```

**RevenueCat Integration**:
```swift
import RevenueCat

class PurchaseManager: ObservableObject {
    @Published var isPremium = false
    @Published var offerings: Offerings?
    
    init() {
        Purchases.logLevel = .debug
        Purchases.configure(withAPIKey: "your_revenuecat_key")
        checkEntitlements()
        loadOfferings()
    }
    
    func checkEntitlements() {
        Purchases.shared.getCustomerInfo { customerInfo, error in
            DispatchQueue.main.async {
                self.isPremium = customerInfo?.entitlements["premium"]?.isActive == true
            }
        }
    }
    
    func loadOfferings() {
        Purchases.shared.getOfferings { offerings, error in
            DispatchQueue.main.async {
                self.offerings = offerings
            }
        }
    }
    
    func purchasePremium() async throws {
        let package = offerings?.current?.availablePackages.first
        try await Purchases.shared.purchase(package: package!)
    }
}
```

### 13. System Features & Deep Linking

**Objective**: Implement system integrations and deep linking.

**Implementation Details**:
```swift
// App Delegate for Deep Linking
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Handle deep links
        if let url = launchOptions?[.url] as? URL {
            handleDeepLink(url)
        }
        
        return true
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        handleDeepLink(url)
        return true
    }
    
    private func handleDeepLink(_ url: URL) {
        if url.host == "shortcut" {
            let stationID = url.lastPathComponent
            // Open specific station
            NotificationCenter.default.post(name: .openStation, object: stationID)
        }
    }
}

// App Shortcuts
class ShortcutManager {
    static func createShortcut(for station: Station) {
        let shortcut = UIMutableApplicationShortcutItem(
            type: "com.srivyaradio.playstation",
            localizedTitle: "Play \(station.name)",
            localizedSubtitle: "Quick play",
            icon: UIApplicationShortcutIcon(systemImageName: "play"),
            userInfo: [
                "stationID": station.id,
                "stationName": station.name
            ] as [String : NSSecureCoding]
        )
        
        UIApplication.shared.shortcutItems = [shortcut]
    }
}
```

### 14. Data Import/Export & Preferences

**Objective**: Implement data management and user preferences.

**Implementation Details**:
```swift
// Country Import/Export
class CountryManager {
    func exportCountries() -> URL? {
        // Export user countries to CSV
        let csvContent = generateCSVContent()
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let fileURL = documentsPath.appendingPathComponent("countries.csv")
        
        do {
            try csvContent.write(to: fileURL, atomically: true, encoding: .utf8)
            return fileURL
        } catch {
            print("Export failed: \(error)")
            return nil
        }
    }
    
    func importCountries(from url: URL) throws {
        let content = try String(contentsOf: url)
        let lines = content.components(separatedBy: .newlines)
        
        for line in lines {
            let components = line.components(separatedBy: ",")
            if components.count >= 2 {
                let name = components[0]
                let code = components[1]
                addCustomCountry(name: name, code: code)
            }
        }
    }
    
    private func generateCSVContent() -> String {
        var csv = "Country Name,Country Code\n"
        // Add user countries
        return csv
    }
}

// Theme Management
class ThemeManager: ObservableObject {
    @Published var currentTheme: AppTheme = .auto
    
    enum AppTheme: String, CaseIterable {
        case light, dark, auto
        
        var userInterfaceStyle: UIUserInterfaceStyle {
            switch self {
            case .light:
                return .light
            case .dark:
                return .dark
            case .auto:
                return .unspecified
            }
        }
    }
    
    func applyTheme() {
        UIApplication.shared.windows.first?.overrideUserInterfaceStyle = currentTheme.userInterfaceStyle
    }
}
```

### 15. Testing & Polish

**Objective**: Comprehensive testing and app store preparation.

**Testing Implementation**:
```swift
// Unit Tests Example
class CoreDataRepositoryTests: XCTestCase {
    var repository: CoreDataRepository!
    
    override func setUp() {
        super.setUp()
        repository = CoreDataRepository()
    }
    
    func testGetStationsForCountry() async {
        let stations = await repository.getStations(for: "US")
        XCTAssertFalse(stations.isEmpty)
    }
    
    func testAddToFavorites() async {
        let testStation = Station(id: "test", favicon: "", name: "Test Radio", country: "US", tags: "", countrycode: "US", url_resolved: "http://test.com", state: "", homepage: "", rank: 0)
        
        await repository.addToFavorites(station: testStation)
        let favorites = await repository.getFavorites()
        
        XCTAssertTrue(favorites.contains { $0.id == testStation.id })
    }
}

// UI Tests Example
class SrivyaRadioUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    func testDiscoverFlow() {
        let app = XCUIApplication()
        
        // Test country selection
        let countryPicker = app.pickers.firstMatch
        XCTAssertTrue(countryPicker.exists)
        
        // Test search functionality
        let searchField = app.searchFields.firstMatch
        searchField.tap()
        searchField.typeText("jazz")
        
        // Test station selection
        let stationCell = app.tables.cells.firstMatch
        XCTAssertTrue(stationCell.exists)
        stationCell.tap()
    }
}
```

## Implementation Timeline

### Phase 1: Foundation (Weeks 1-2)
- Project setup and architecture analysis
- Core Data model creation
- Basic networking layer
- Repository pattern implementation

### Phase 2: Core Features (Weeks 3-4)
- Media player implementation
- Main UI screens
- Navigation structure
- Search and filtering

### Phase 3: Advanced Features (Weeks 5-6)
- Background audio and downloads
- Favorites management
- Sleep timer and advanced controls
- Offline content handling

### Phase 4: Integrations (Weeks 7-8)
- CarPlay integration
- Google Ads implementation
- RevenueCat integration
- System features (shortcuts, deep linking)

### Phase 5: Polish & Testing (Weeks 9-10)
- Comprehensive testing
- Performance optimization
- App store preparation
- Final documentation

## Technical Considerations

### iOS-Specific Features
- **Background App Refresh**: For updating station data
- **Background Audio**: For continuous playback
- **CarPlay Integration**: For in-car experience
- **App Shortcuts**: For quick station access
- **Spotlight Search**: For station discovery

### Performance Optimizations
- **Lazy Loading**: For large station lists
- **Image Caching**: For station artwork
- **Database Indexing**: For fast search
- **Memory Management**: For audio playback

### App Store Guidelines Compliance
- Background audio usage justification
- Privacy policy for data collection
- Content rating for radio streaming
- In-app purchase guidelines compliance

## Conclusion

This comprehensive plan provides a detailed roadmap for porting the SrivyaRadio Android application to iOS. The approach maintains feature parity while leveraging iOS-specific capabilities and design patterns. The implementation follows modern iOS development practices with SwiftUI, async/await, and Core Data.

The plan is structured to minimize risk while ensuring a high-quality iOS experience that matches or exceeds the Android version's functionality.