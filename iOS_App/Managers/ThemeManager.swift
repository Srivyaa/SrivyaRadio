import SwiftUI
import UIKit

// MARK: - Theme Manager
class ThemeManager: ObservableObject {
    @Published var currentTheme: AppTheme = .auto {
        didSet {
            applyTheme()
            saveTheme()
        }
    }
    
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
        
        var displayName: String {
            switch self {
            case .light:
                return "Light"
            case .dark:
                return "Dark"
            case .auto:
                return "Auto"
            }
        }
        
        var iconName: String {
            switch self {
            case .light:
                return "sun.max.fill"
            case .dark:
                return "moon.fill"
            case .auto:
                return "circle.lefthalf.filled"
            }
        }
    }
    
    private let userDefaultsKey = "themeMode"
    private let sharedPreferencesRepository = SharedPreferencesRepository()
    
    init() {
        loadTheme()
    }
    
    private func loadTheme() {
        let savedTheme = sharedPreferencesRepository.getThemeMode()
        if let theme = AppTheme(rawValue: savedTheme) {
            currentTheme = theme
        }
        applyTheme()
    }
    
    private func saveTheme() {
        sharedPreferencesRepository.setThemeMode(currentTheme.rawValue)
    }
    
    func setTheme(_ theme: AppTheme) {
        currentTheme = theme
    }
    
    func applyTheme() {
        UIApplication.shared.windows.first?.overrideUserInterfaceStyle = currentTheme.userInterfaceStyle
        
        // Update navigation bar appearance
        let navBarAppearance = UINavigationBarAppearance()
        switch currentTheme {
        case .light:
            navBarAppearance.configureWithOpaqueBackground()
            navBarAppearance.backgroundColor = UIColor.systemBackground
            navBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.label]
            navBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.label]
        case .dark:
            navBarAppearance.configureWithOpaqueBackground()
            navBarAppearance.backgroundColor = UIColor.systemBackground
            navBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.label]
            navBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.label]
        case .auto:
            navBarAppearance.configureWithDefaultBackground()
            navBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.label]
            navBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.label]
        }
        
        UINavigationBar.appearance().standardAppearance = navBarAppearance
        UINavigationBar.appearance().compactAppearance = navBarAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = navBarAppearance
        
        // Update tab bar appearance
        let tabBarAppearance = UITabBarAppearance()
        tabBarAppearance.configureWithOpaqueBackground()
        tabBarAppearance.backgroundColor = UIColor.systemBackground
        
        UITabBar.appearance().standardAppearance = tabBarAppearance
        UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
        
        // Update toolbar appearance
        let toolbarAppearance = UIToolbarAppearance()
        toolbarAppearance.configureWithOpaqueBackground()
        toolbarAppearance.backgroundColor = UIColor.systemBackground
        
        UIToolbar.appearance().standardAppearance = toolbarAppearance
        UIToolbar.appearance().scrollEdgeAppearance = toolbarAppearance
    }
    
    static func isDarkMode() -> Bool {
        return UITraitCollection.current.userInterfaceStyle == .dark
    }
}

// MARK: - Shared Preferences Repository
class SharedPreferencesRepository {
    private let userDefaults = UserDefaults.standard
    
    // Theme management
    func getThemeMode() -> String {
        return userDefaults.string(forKey: "themeMode") ?? "auto"
    }
    
    func setThemeMode(_ mode: String) {
        userDefaults.set(mode, forKey: "themeMode")
    }
    
    // Country selection
    func getUserCountry() -> String {
        return userDefaults.string(forKey: "userCountry") ?? "US"
    }
    
    func setUserCountry(_ country: String) {
        userDefaults.set(country, forKey: "userCountry")
    }
    
    // Default screen preference
    func getDefaultScreen() -> String? {
        return userDefaults.string(forKey: "defaultScreen")
    }
    
    func setDefaultScreen(_ screen: String) {
        userDefaults.set(screen, forKey: "defaultScreen")
    }
    
    // User countries (custom country list)
    func getUserCountries() -> [[String: String]] {
        guard let data = userDefaults.data(forKey: "userCountries"),
              let countries = try? JSONSerialization.jsonObject(with: data) as? [[String: String]] else {
            return []
        }
        return countries
    }
    
    func setUserCountries(_ countries: [[String: String]]) {
        if let data = try? JSONSerialization.data(withJSONObject: countries) {
            userDefaults.set(data, forKey: "userCountries")
        }
    }
    
    // Recent stations
    func getRecents() -> [String] {
        return userDefaults.stringArray(forKey: "recents") ?? []
    }
    
    func addRecent(_ stationId: String) {
        var recents = getRecents()
        recents.removeAll { $0 == stationId }
        recents.insert(stationId, at: 0)
        
        // Keep only last 50 recents
        if recents.count > 50 {
            recents = Array(recents.prefix(50))
        }
        
        userDefaults.set(recents, forKey: "recents")
    }
    
    // First startup
    func isFirstStartup() -> Bool {
        return !userDefaults.bool(forKey: "hasLaunchedBefore")
    }
    
    func setFirstStartupCompleted() {
        userDefaults.set(true, forKey: "hasLaunchedBefore")
    }
    
    // Last played station
    func getLastPlayID() -> String? {
        return userDefaults.string(forKey: "lastPlayID")
    }
    
    func setLastPlayID(_ stationID: String) {
        userDefaults.set(stationID, forKey: "lastPlayID")
    }
}