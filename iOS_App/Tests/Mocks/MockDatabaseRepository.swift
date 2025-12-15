import Foundation
import Combine

class MockDatabaseRepository: DatabaseRepositoryProtocol {
    var stations: [Station] = []
    var favorites: [Station] = []
    var downloadedItems: [DownloadedItem] = []
    var recentStations: [Station] = []
    
    // MARK: - Station Operations
    func getStations(for country: String) async -> [Station] {
        return stations.filter { $0.country == country }
    }
    
    func searchStations(query: String) async -> [Station] {
        return stations.filter { $0.name.contains(query) }
    }
    
    func saveStations(_ stations: [Station], countryCode: String) async {
        self.stations.append(contentsOf: stations)
    }
    
    func getStation(by id: String) async -> Station? {
        return stations.first { $0.id == id }
    }
    
    // MARK: - Favorites
    func getFavorites() async -> [Station] {
        return favorites
    }
    
    func addToFavorites(_ station: Station) async {
        if !favorites.contains(where: { $0.id == station.id }) {
            favorites.append(station)
        }
    }
    
    func removeFromFavorites(_ station: Station) async {
        favorites.removeAll { $0.id == station.id }
    }
    
    func isFavorite(_ stationId: String) async -> Bool {
        return favorites.contains(where: { $0.id == stationId })
    }
    
    // MARK: - Downloads
    func addDownloadedItem(_ item: DownloadedItem) async {
        downloadedItems.append(item)
    }
    
    func getDownloadedItems() async -> [DownloadedItem] {
        return downloadedItems
    }
    
    func removeDownloadedItem(_ item: DownloadedItem) async {
         downloadedItems.removeAll { $0.id == item.id }
    }
    
    func removeDownloadedItem(_ itemId: String) async {
        downloadedItems.removeAll { $0.id == itemId }
    }
    
    func getDownloadedItem(by sourceUrl: String) async -> DownloadedItem? {
        return downloadedItems.first { $0.sourceUrl == sourceUrl }
    }
    
    // MARK: - Recents
    func addRecentStation(_ station: Station) async {
        recentStations.removeAll { $0.id == station.id }
        recentStations.insert(station, at: 0)
    }
    
    func getRecentStations() async -> [Station] {
        return recentStations
    }
    
    func clearRecentStations() async {
        recentStations.removeAll()
    }
}
