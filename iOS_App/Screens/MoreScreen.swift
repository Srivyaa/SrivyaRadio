import SwiftUI

// MARK: - More Screen View Model
class MoreScreenViewModel: ObservableObject {
    @Published var isPremium = false
    @Published var themeMode: ThemeManager.AppTheme = .auto
    
    private let purchaseManager: PurchaseManager
    private let themeManager: ThemeManager
    
    init(purchaseManager: PurchaseManager = PurchaseManager(),
         themeManager: ThemeManager = ThemeManager()) {
        self.purchaseManager = purchaseManager
        self.themeManager = themeManager
        
        // Observe premium status
        purchaseManager.$isPremium
            .receive(on: DispatchQueue.main)
            .assign(to: \.isPremium, on: self)
            .store(in: &CancellableSet())
        
        // Observe theme changes
        themeManager.$currentTheme
            .receive(on: DispatchQueue.main)
            .assign(to: \.themeMode, on: self)
            .store(in: &CancellableSet())
    }
    
    func purchasePremium() {
        Task {
            do {
                try await purchaseManager.purchasePremium()
            } catch {
                print("Purchase failed: \(error)")
            }
        }
    }
    
    func restorePurchases() {
        Task {
            do {
                try await purchaseManager.restorePurchases()
            } catch {
                print("Restore failed: \(error)")
            }
        }
    }
    
    func changeTheme(_ theme: ThemeManager.AppTheme) {
        themeManager.setTheme(theme)
    }
}

// MARK: - More Screen
struct MoreScreen: View {
    @EnvironmentObject var mediaPlayerManager: MediaPlayerManager
    @StateObject private var viewModel = MoreScreenViewModel()
    
    var body: some View {
        List {
            // Premium Section
            Section("Premium") {
                if viewModel.isPremium {
                    HStack {
                        Image(systemName: "crown.fill")
                            .foregroundColor(.yellow)
                        VStack(alignment: .leading) {
                            Text("Premium Active")
                                .font(.headline)
                            Text("Enjoy ad-free listening and exclusive features")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                    }
                } else {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "crown")
                                .foregroundColor(.yellow)
                            Text("Upgrade to Premium")
                                .font(.headline)
                        }
                        Text("• Ad-free listening experience\n• Download stations for offline use\n• Priority customer support")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Button(action: {
                            viewModel.purchasePremium()
                        }) {
                            Text("Upgrade Now")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .cornerRadius(8)
                        }
                    }
                }
            }
            
            // Player Controls Section
            Section("Player") {
                HStack {
                    Image(systemName: "timer")
                    Text("Sleep Timer")
                    Spacer()
                    Button("Set") {
                        // Show sleep timer options
                        showSleepTimerSheet = true
                    }
                }
                
                HStack {
                    Image(systemName: "shuffle")
                    Text("Shuffle")
                    Spacer()
                    Toggle("", isOn: Binding(
                        get: { mediaPlayerManager.isShuffleEnabled },
                        set: { mediaPlayerManager.toggleShuffle(enabled: $0) }
                    ))
                }
                
                HStack {
                    Image(systemName: "repeat")
                    Text("Repeat")
                    Spacer()
                    Picker("Repeat Mode", selection: Binding(
                        get: { mediaPlayerManager.repeatMode },
                        set: { mediaPlayerManager.setRepeatMode($0) }
                    )) {
                        Text("Off").tag(MediaPlayerManager.RepeatMode.off)
                        Text("One").tag(MediaPlayerManager.RepeatMode.one)
                        Text("All").tag(MediaPlayerManager.RepeatMode.all)
                    }
                    .pickerStyle(.menu)
                }
            }
            
            // Library Section
            Section("Library") {
                NavigationLink(destination: RecentsScreen()) {
                     Label("Recently Played", systemImage: "clock")
                }
                NavigationLink(destination: QueueScreen()) {
                     Label("Play Queue", systemImage: "music.note.list")
                }
            }
            
            // Settings Section
            Section("Settings") {
                // Theme Selection
                NavigationLink(destination: ThemeSettingsView()) {
                    HStack {
                        Image(systemName: "paintbrush")
                        VStack(alignment: .leading) {
                            Text("Theme")
                            Text(viewModel.themeMode.rawValue.capitalized)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // Default Screen
                NavigationLink(destination: DefaultScreenSettingsView()) {
                    HStack {
                        Image(systemName: "house")
                        VStack(alignment: .leading) {
                            Text("Default Screen")
                            Text("Choose startup screen")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // Download Settings
                NavigationLink(destination: DownloadSettingsView()) {
                    HStack {
                        Image(systemName: "arrow.down.circle")
                        VStack(alignment: .leading) {
                            Text("Downloads")
                            Text("Manage offline content")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // Country Management
                NavigationLink(destination: CountryManagementView()) {
                    HStack {
                        Image(systemName: "globe")
                        VStack(alignment: .leading) {
                            Text("Countries")
                            Text("Manage country list")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            
            // Information Section
            Section("Information") {
                NavigationLink(destination: AboutScreen()) {
                    HStack {
                        Image(systemName: "info.circle")
                        Text("About")
                    }
                }
                
                NavigationLink(destination: PrivacyPolicyView()) {
                    HStack {
                        Image(systemName: "hand.raised")
                        Text("Privacy Policy")
                    }
                }
                
                NavigationLink(destination: TermsOfServiceView()) {
                    HStack {
                        Image(systemName: "doc.text")
                        Text("Terms of Service")
                    }
                }
                
                Link(destination: URL(string: "mailto:support@srivyaradio.com")!) {
                    HStack {
                        Image(systemName: "envelope")
                        Text("Contact Support")
                    }
                }
            }
            
            // Advanced Section
            Section("Advanced") {
                Button(action: {
                    showingClearCacheAlert = true
                }) {
                    HStack {
                        Image(systemName: "trash")
                        Text("Clear Cache")
                            .foregroundColor(.red)
                    }
                }
                
                Button(action: {
                    showingExportDataSheet = true
                }) {
                    HStack {
                        Image(systemName: "square.and.arrow.up")
                        Text("Export Data")
                    }
                }
                
                Button(action: {
                    viewModel.restorePurchases()
                }) {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                        Text("Restore Purchases")
                    }
                }
            }
        }
        .navigationTitle("More")
        .sheet(isPresented: $showSleepTimerSheet) {
            SleepTimerView()
        }
        .alert("Clear Cache", isPresented: $showingClearCacheAlert) {
            Button("Clear", role: .destructive) {
                clearCache()
            }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("This will clear cached station data and images. Your favorites and settings will not be affected.")
        }
        .sheet(isPresented: $showingExportDataSheet) {
            ExportDataView()
        }
    }
    
    @State private var showSleepTimerSheet = false
    @State private var showingClearCacheAlert = false
    @State private var showingExportDataSheet = false
    
    private func clearCache() {
        // Implementation for clearing cache
        URLCache.shared.removeAllCachedResponses()
        print("Cache cleared")
    }
}

// MARK: - Supporting Views
struct ThemeSettingsView: View {
    @EnvironmentObject var themeManager: ThemeManager
    
    var body: some View {
        List {
            ForEach(ThemeManager.AppTheme.allCases, id: \.self) { theme in
                Button(action: {
                    themeManager.setTheme(theme)
                }) {
                    HStack {
                        Text(theme.rawValue.capitalized)
                        Spacer()
                        if themeManager.currentTheme == theme {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                }
            }
        }
        .navigationTitle("Theme")
    }
}

struct DefaultScreenSettingsView: View {
    @State private var defaultScreen = "discover"
    
    var body: some View {
        List {
            Picker("Default Screen", selection: $defaultScreen) {
                Text("Discover").tag("discover")
                Text("Favorites").tag("favorites")
                Text("More").tag("more")
            }
            .pickerStyle(.radioGroup)
        }
        .navigationTitle("Default Screen")
    }
}

struct DownloadSettingsView: View {
    @EnvironmentObject var databaseRepository: DatabaseRepositoryProtocol
    @StateObject private var downloadManager = DownloadManager.shared
    @State private var downloadedItems: [DownloadedItem] = []
    
    var body: some View {
        List {
            Section {
                Button(action: {
                    Task {
                        downloadedItems = await databaseRepository.getDownloadedItems()
                    }
                }) {
                    Text("Refresh Downloads")
                }
            }
            
            Section("Downloaded Content") {
                if downloadedItems.isEmpty {
                    Text("No downloaded content")
                        .foregroundColor(.secondary)
                        .italic()
                } else {
                    ForEach(downloadedItems) { item in
                        VStack(alignment: .leading) {
                            Text(item.name)
                                .font(.headline)
                            Text(item.countrycode)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .onDelete(perform: deleteItems)
                }
            }
        }
        .navigationTitle("Downloads")
        .onAppear {
            Task {
                downloadedItems = await databaseRepository.getDownloadedItems()
            }
        }
    }
    
    private func deleteItems(at offsets: IndexSet) {
        for index in offsets {
            let item = downloadedItems[index]
            downloadManager.deleteDownload(item)
        }
        downloadedItems.remove(atOffsets: offsets)
    }
}

struct CountryManagementView: View {
    var body: some View {
        List {
            Text("Country management features will be implemented here.")
                .foregroundColor(.secondary)
        }
        .navigationTitle("Countries")
    }
}

struct PrivacyPolicyView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Privacy Policy")
                    .font(.title)
                    .fontWeight(.bold)
                
                // Privacy policy content would go here
                Text("""
                This privacy policy describes how SrivyaRadio collects, uses, and protects your information.
                
                Information We Collect:
                • Station favorites and listening history
                • App usage statistics
                • Device information for crash reporting
                
                How We Use Your Information:
                • To provide and improve our service
                • To sync your favorites across devices
                • To provide customer support
                
                Data Security:
                We implement appropriate security measures to protect your information.
                """)
                .font(.body)
                .multilineTextAlignment(.leading)
            }
            .padding()
        }
        .navigationTitle("Privacy Policy")
    }
}

struct TermsOfServiceView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Terms of Service")
                    .font(.title)
                    .fontWeight(.bold)
                
                // Terms of service content would go here
                Text("""
                By using SrivyaRadio, you agree to these terms:
                
                1. Use of Service
                SrivyaRadio provides internet radio streaming services for personal, non-commercial use.
                
                2. Content
                All radio station content is provided by third-party sources. We do not host or control the streamed content.
                
                3. Premium Features
                Premium features are provided on a subscription basis and may be cancelled at any time.
                
                4. Limitation of Liability
                We are not liable for any issues arising from the use of this application.
                """)
                .font(.body)
                .multilineTextAlignment(.leading)
            }
            .padding()
        }
        .navigationTitle("Terms of Service")
    }
}

struct SleepTimerView: View {
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            List {
                ForEach([15, 30, 45, 60, 90], id: \.self) { minutes in
                    Button(action: {
                        // Set sleep timer
                        dismiss()
                    }) {
                        Text("\(minutes) minutes")
                    }
                }
                
                Button("Cancel Timer") {
                    // Cancel sleep timer
                    dismiss()
                }
                .foregroundColor(.red)
            }
            .navigationTitle("Sleep Timer")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct ExportDataView: View {
    @Environment(\.dismiss) var dismiss
    @State private var isProcessing = false
    @State private var showShareSheet = false
    @State private var exportURL: URL?
    @State private var showFileImporter = false
    @State private var alertMessage = ""
    @State private var showAlert = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Image(systemName: "arrow.triangle.2.circlepath")
                    .font(.system(size: 60))
                    .foregroundColor(.blue)
                
                Text("Backup & Restore")
                    .font(.title)
                    .fontWeight(.bold)
                
                Text("""
                Export your favorites to keep them safe or transfer them to another device. Import a CSV file to restore them.
                """)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
                .padding()
                
                // Export Button
                Button(action: {
                    exportData()
                }) {
                    if isProcessing {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle())
                    } else {
                        HStack {
                            Image(systemName: "square.and.arrow.up")
                            Text("Export Favorites")
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(8)
                    }
                }
                .disabled(isProcessing)
                
                // Import Button
                Button(action: {
                    showFileImporter = true
                }) {
                    HStack {
                        Image(systemName: "square.and.arrow.down")
                        Text("Import Favorites")
                    }
                    .font(.headline)
                    .foregroundColor(.blue)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(8)
                }
                .disabled(isProcessing)
                
                Spacer()
            }
            .padding()
            .navigationTitle("Data Management")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .sheet(isPresented: $showShareSheet) {
                if let url = exportURL {
                    ShareSheet(activityItems: [url])
                }
            }
            .fileImporter(isPresented: $showFileImporter, allowedContentTypes: [.commaSeparatedText, .plainText]) { result in
                switch result {
                case .success(let url):
                    importData(url: url)
                case .failure(let error):
                    print("Import failed: \(error.localizedDescription)")
                }
            }
            .alert("Data Import", isPresented: $showAlert) {
                Button("OK", role: .cancel) { }
            } message: {
                Text(alertMessage)
            }
        }
    }
    
    private func exportData() {
        isProcessing = true
        Task {
            do {
                let url = try await DataExportManager.shared.exportFavoritesToCSV()
                exportURL = url
                showShareSheet = true
            } catch {
                print("Export failed: \(error)")
            }
            isProcessing = false
        }
    }
    
    private func importData(url: URL) {
        isProcessing = true
        Task {
            do {
                let count = try await DataExportManager.shared.importFavoritesFromCSV(url: url)
                alertMessage = "Successfully imported \(count) favorites."
                showAlert = true
            } catch {
                alertMessage = "Import failed: \(error.localizedDescription)"
                showAlert = true
            }
            isProcessing = false
        }
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    var activityItems: [Any]
    var applicationActivities: [UIActivity]? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
        return controller
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}