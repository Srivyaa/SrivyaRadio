import SwiftUI

// MARK: - Discover View Model
class DiscoverViewModel: ObservableObject {
    @Published var stations: [Station] = []
    @Published var searchText: String = ""
    @Published var selectedCountry: String = "US"
    @Published var isLoading = false
    @Published var countries: [Country] = []
    @Published var isRefreshing = false
    
    private let databaseRepository: DatabaseRepositoryProtocol
    private let stationsClient: StationsClientProtocol
    private let locationClient = LocationClient.shared
    private var searchTask: Task<Void, Never>?
    
    init(databaseRepository: DatabaseRepositoryProtocol = CoreDataRepository(),
         stationsClient: StationsClientProtocol = StationsClient.shared) {
        self.databaseRepository = databaseRepository
        self.stationsClient = stationsClient
        
        // Initialize countries list
        initializeCountries()
        loadInitialData()
    }
    
    private func initializeCountries() {
        // Load country list - this would typically come from a constants file
        countries = [
            Country(name: "United States", code: "US"),
            Country(name: "United Kingdom", code: "GB"),
            Country(name: "Canada", code: "CA"),
            Country(name: "Australia", code: "AU"),
            Country(name: "Germany", code: "DE"),
            Country(name: "France", code: "FR"),
            Country(name: "India", code: "IN"),
            Country(name: "Japan", code: "JP"),
            Country(name: "Brazil", code: "BR"),
            Country(name: "Mexico", code: "MX")
        ]
    }
    
    private func loadInitialData() {
        Task {
            await loadStations(for: selectedCountry)
            await detectUserCountry()
        }
    }
    
    private func detectUserCountry() async {
        do {
            let countryCode = try await locationClient.getCountryCode()
            await MainActor.run {
                if countries.contains(where: { $0.code == countryCode }) {
                    selectedCountry = countryCode
                    Task {
                        await loadStations(for: countryCode)
                    }
                }
            }
        } catch {
            print("Failed to detect country: \(error)")
        }
    }
    
    func loadStations(for countryCode: String) async {
        await MainActor.run {
            isLoading = true
        }
        
        // First check if stations exist in database
        var stations = await databaseRepository.getStations(for: countryCode)
        
        // If no stations in database, fetch from API
        if stations.isEmpty {
            await fetchStationsFromAPI(for: countryCode)
            stations = await databaseRepository.getStations(for: countryCode)
        }
        
        await MainActor.run {
            self.stations = stations
            self.isLoading = false
            self.selectedCountry = countryCode
        }
    }
    
    private func fetchStationsFromAPI(for countryCode: String) async {
        do {
            let apiStations = try await stationsClient.getStations(for: countryCode)
            await databaseRepository.saveStations(apiStations, countryCode: countryCode)
        } catch {
            print("Failed to fetch stations for \(countryCode): \(error)")
        }
    }
    
    func refreshStations() async {
        await MainActor.run {
            isRefreshing = true
        }
        
        await fetchStationsFromAPI(for: selectedCountry)
        let updatedStations = await databaseRepository.getStations(for: selectedCountry)
        
        await MainActor.run {
            self.stations = updatedStations
            self.isRefreshing = false
        }
    }
    
    func searchStations(query: String) async {
        // Cancel previous search task
        searchTask?.cancel()
        
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedQuery.isEmpty else {
            await MainActor.run {
                stations = []
            }
            return
        }
        
        searchTask = Task {
            await MainActor.run {
                isLoading = true
            }
            
            let results = await databaseRepository.searchStations(query: trimmedQuery)
            
            await MainActor.run {
                if !Task.isCancelled {
                    self.stations = results
                    self.isLoading = false
                }
            }
        }
    }
    
    func countryChanged(to countryCode: String) {
        Task {
            await loadStations(for: countryCode)
        }
    }
    
    func refreshCountry() {
        Task {
            await refreshStations()
        }
    }
}

// MARK: - Country Model
struct Country: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let code: String
}

// MARK: - Discover Screen
struct DiscoverScreen: View {
    @EnvironmentObject var mediaPlayerManager: MediaPlayerManager
    @EnvironmentObject var databaseRepository: DatabaseRepositoryProtocol
    @StateObject private var viewModel = DiscoverViewModel()
    
    var body: some View {
        List {
            // Country Selection
            Section("Country") {
                Picker("Select Country", selection: $viewModel.selectedCountry) {
                    ForEach(viewModel.countries) { country in
                        Text(country.name).tag(country.code)
                    }
                }
                .pickerStyle(.menu)
                .onChange(of: viewModel.selectedCountry) { newCountry in
                    viewModel.countryChanged(to: newCountry)
                }
                
                // Refresh button
                Button(action: {
                    viewModel.refreshCountry()
                }) {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                        Text("Refresh \(viewModel.selectedCountry)")
                    }
                }
            }
            
            // Search Section
            Section("Search") {
                HStack {
                    Image(systemName: "magnifyingglass")
                    TextField("Search stations...", text: $viewModel.searchText)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    if !viewModel.searchText.isEmpty {
                        Button(action: {
                            viewModel.searchText = ""
                            Task {
                                await viewModel.searchStations(query: "")
                            }
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .onChange(of: viewModel.searchText) { searchText in
                    Task {
                        await viewModel.searchStations(query: searchText)
                    }
                }
            }
            
            // Stations List
            Section {
                if viewModel.isLoading {
                    ProgressView("Loading stations...")
                        .frame(maxWidth: .infinity, alignment: .center)
                        .listRowBackground(Color.clear)
                } else if viewModel.stations.isEmpty {
                    Text("No stations found")
                        .foregroundColor(.secondary)
                        .italic()
                        .frame(maxWidth: .infinity, alignment: .center)
                        .listRowBackground(Color.clear)
                } else {
                    ForEach(viewModel.stations) { station in
                        StationRow(station: station)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                // Set queue and play
                                if let index = viewModel.stations.firstIndex(where: { $0.id == station.id }) {
                                    mediaPlayerManager.setQueue(viewModel.stations, startingIndex: index)
                                } else {
                                    mediaPlayerManager.playStation(station)
                                }
                                
                                // Add to recents
                                Task {
                                    await databaseRepository.addRecentStation(station)
                                }
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                // Favorite action
                                Button(action: {
                                    toggleFavorite(station: station)
                                }) {
                                    Image(systemName: "heart")
                                }
                                .tint(.blue)
                                
                                // Download action (if supported)
                                if isDownloadable(station: station) {
                                    Button(action: {
                                        downloadStation(station: station)
                                    }) {
                                        Image(systemName: "arrow.down.circle")
                                    }
                                    .tint(.green)
                                }
                            }
                    }
                }
            } header: {
                Text(viewModel.searchText.isEmpty ? "Stations" : "Search Results")
            }
        }
        .refreshable {
            await viewModel.refreshStations()
        }
        .navigationTitle("Discover")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                if viewModel.isRefreshing {
                    ProgressView()
                        .scaleEffect(0.8)
                }
            }
        }
    }
    
    private func toggleFavorite(station: Station) {
        Task {
            let isFavorite = await databaseRepository.isFavorite(station.id)
            if isFavorite {
                await databaseRepository.removeFromFavorites(station)
            } else {
                await databaseRepository.addToFavorites(station)
            }
        }
    }
    
    private func downloadStation(station: Station) {
        DownloadManager.shared.startDownload(for: station)
    }
    
    private func isDownloadable(station: Station) -> Bool {
        let url = station.url_resolved.lowercased()
        return url.hasSuffix(".mp3") || url.hasSuffix(".aac") || url.hasSuffix(".m4a")
    }
}

// MARK: - Station Row Component
struct StationRow: View {
    let station: Station
    
    var body: some View {
        HStack(spacing: 12) {
            // Station artwork
            AsyncImage(url: URL(string: station.favicon)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                ZStack {
                    Rectangle()
                        .fill(Color.gray.opacity(0.3))
                        .overlay(
                            Image(systemName: "radio")
                                .foregroundColor(.gray)
                                .font(.system(size: 20))
                        )
                }
            }
            .frame(width: 50, height: 50)
            .cornerRadius(8)
            .clipped()
            
            VStack(alignment: .leading, spacing: 4) {
                Text(station.name)
                    .font(.headline)
                    .lineLimit(2)
                
                Text(station.country)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                if !station.tags.isEmpty {
                    Text(station.tags)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
            
            Spacer()
            
            // Play indicator
            Image(systemName: "play.circle.fill")
                .foregroundColor(.blue)
                .font(.title3)
        }
        .padding(.vertical, 4)
    }
}