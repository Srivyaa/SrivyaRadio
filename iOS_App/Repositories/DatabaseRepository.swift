import Foundation
import CoreData
import Combine

// MARK: - Repository Protocol
protocol DatabaseRepositoryProtocol: ObservableObject {
    // Station operations
    func getStations(for country: String) async -> [Station]
    func searchStations(query: String) async -> [Station]
    func getStation(by id: String) async -> Station?
    func saveStations(_ stations: [Station], countryCode: String) async
    
    // Favorite operations
    func getFavorites() async -> [Station]
    func addToFavorites(_ station: Station) async
    func removeFromFavorites(_ station: Station) async
    func isFavorite(_ stationId: String) async -> Bool
    
    // Downloaded items operations
    func getDownloadedItems() async -> [DownloadedItem]
    func addDownloadedItem(_ item: DownloadedItem) async
    func removeDownloadedItem(_ item: DownloadedItem) async
    
    // Recent stations
    func addRecentStation(_ station: Station) async
    func getRecentStations() async -> [Station]
    func clearRecentStations() async
}

// MARK: - Repository Implementation
class CoreDataRepository: ObservableObject, DatabaseRepositoryProtocol {
    private let coreDataManager = CoreDataManager.shared
    
    // MARK: - Station Operations
    func getStations(for country: String) async -> [Station] {
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                request.predicate = NSPredicate(format: "countrycode == %@", country.uppercased())
                request.sortDescriptors = [NSSortDescriptor(key: "name", ascending: true)]
                
                do {
                    let results = try context.fetch(request)
                    let stations = results.map { $0.toStation() }
                    continuation.resume(returning: stations)
                } catch {
                    print("Error fetching stations: \(error)")
                    continuation.resume(returning: [])
                }
            }
        }
    }
    
    func searchStations(query: String) async -> [Station] {
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedQuery.isEmpty else { return [] }
        
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                let namePredicate = NSPredicate(format: "name CONTAINS[cd] %@", trimmedQuery)
                let countryPredicate = NSPredicate(format: "country CONTAINS[cd] %@", trimmedQuery)
                let tagsPredicate = NSPredicate(format: "tags CONTAINS[cd] %@", trimmedQuery)
                
                request.predicate = NSCompoundPredicate(orPredicateWithSubpredicates: [
                    namePredicate,
                    countryPredicate,
                    tagsPredicate
                ])
                request.sortDescriptors = [NSSortDescriptor(key: "name", ascending: true)]
                
                do {
                    let results = try context.fetch(request)
                    let stations = results.map { $0.toStation() }
                    continuation.resume(returning: stations)
                } catch {
                    print("Error searching stations: \(error)")
                    continuation.resume(returning: [])
                }
            }
        }
    }
    
    func getStation(by id: String) async -> Station? {
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                request.predicate = NSPredicate(format: "id == %@", id)
                
                do {
                    let results = try context.fetch(request)
                    let station = results.first?.toStation()
                    continuation.resume(returning: station)
                } catch {
                    print("Error fetching station: \(error)")
                    continuation.resume(returning: nil)
                }
            }
        }
    }
    
    func saveStations(_ stations: [Station], countryCode: String) async {
        await withCheckedContinuation { continuation in
            coreDataManager.performBackgroundTask { context in
                // Delete existing stations for this country
                let deleteRequest: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                deleteRequest.predicate = NSPredicate(format: "countrycode == %@", countryCode.uppercased())
                
                do {
                    let deleteResults = try context.fetch(deleteRequest)
                    for entity in deleteResults {
                        context.delete(entity)
                    }
                    
                    // Save new stations
                    for station in stations {
                        let entity = StationEntity(context: context)
                        entity.id = station.id
                        entity.favicon = station.favicon
                        entity.name = station.name
                        entity.country = station.country
                        entity.tags = station.tags
                        entity.countrycode = station.countrycode
                        entity.url_resolved = station.url_resolved
                        entity.state = station.state
                        entity.homepage = station.homepage
                        entity.rank = Int32(station.rank)
                        entity.createdAt = Date()
                    }
                    
                    try context.save()
                    continuation.resume()
                } catch {
                    print("Error saving stations: \(error)")
                    continuation.resume()
                }
            }
        }
    }
    
    // MARK: - Favorite Operations
    func getFavorites() async -> [Station] {
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let favoriteRequest: NSFetchRequest<FavoriteEntity> = FavoriteEntity.fetchRequest()
                favoriteRequest.sortDescriptors = [NSSortDescriptor(key: "order", ascending: true)]
                
                do {
                    let favoriteResults = try context.fetch(favoriteRequest)
                    var stations: [Station] = []
                    
                    for favorite in favoriteResults {
                        let stationRequest: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                        stationRequest.predicate = NSPredicate(format: "id == %@", favorite.stationId ?? "")
                        
                        if let stationEntity = try context.fetch(stationRequest).first {
                            stations.append(stationEntity.toStation())
                        }
                    }
                    
                    continuation.resume(returning: stations)
                } catch {
                    print("Error fetching favorites: \(error)")
                    continuation.resume(returning: [])
                }
            }
        }
    }
    
    func addToFavorites(_ station: Station) async {
        await withCheckedContinuation { continuation in
            coreDataManager.performBackgroundTask { context in
                // Check if already exists
                let favoriteRequest: NSFetchRequest<FavoriteEntity> = FavoriteEntity.fetchRequest()
                favoriteRequest.predicate = NSPredicate(format: "stationId == %@", station.id)
                
                do {
                    let existing = try context.fetch(favoriteRequest)
                    guard existing.isEmpty else { continuation.resume(); return }
                    
                    // Get next order
                    let orderRequest: NSFetchRequest<FavoriteEntity> = FavoriteEntity.fetchRequest()
                    let orderResults = try context.fetch(orderRequest)
                    let nextOrder = Int32(orderResults.count)
                    
                    // Create favorite
                    let favorite = FavoriteEntity(context: context)
                    favorite.id = UUID().uuidString
                    favorite.stationId = station.id
                    favorite.order = nextOrder
                    favorite.createdAt = Date()
                    
                    try context.save()
                    continuation.resume()
                } catch {
                    print("Error adding to favorites: \(error)")
                    continuation.resume()
                }
            }
        }
    }
    
    func removeFromFavorites(_ station: Station) async {
        await withCheckedContinuation { continuation in
            coreDataManager.performBackgroundTask { context in
                let request: NSFetchRequest<FavoriteEntity> = FavoriteEntity.fetchRequest()
                request.predicate = NSPredicate(format: "stationId == %@", station.id)
                
                do {
                    let results = try context.fetch(request)
                    for favorite in results {
                        context.delete(favorite)
                    }
                    
                    try context.save()
                    continuation.resume()
                } catch {
                    print("Error removing from favorites: \(error)")
                    continuation.resume()
                }
            }
        }
    }
    
    func isFavorite(_ stationId: String) async -> Bool {
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<FavoriteEntity> = FavoriteEntity.fetchRequest()
                request.predicate = NSPredicate(format: "stationId == %@", stationId)
                
                do {
                    let results = try context.fetch(request)
                    continuation.resume(returning: !results.isEmpty)
                } catch {
                    print("Error checking favorite status: \(error)")
                    continuation.resume(returning: false)
                }
            }
        }
    }
    
    // MARK: - Downloaded Items Operations
    func getDownloadedItems() async -> [DownloadedItem] {
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<DownloadedItemEntity> = DownloadedItemEntity.fetchRequest()
                request.sortDescriptors = [NSSortDescriptor(key: "createdAt", ascending: false)]
                
                do {
                    let results = try context.fetch(request)
                    let items = results.map { $0.toDownloadedItem() }
                    continuation.resume(returning: items)
                } catch {
                    print("Error fetching downloaded items: \(error)")
                    continuation.resume(returning: [])
                }
            }
        }
    }
    
    func addDownloadedItem(_ item: DownloadedItem) async {
        await withCheckedContinuation { continuation in
            coreDataManager.performBackgroundTask { context in
                let entity = DownloadedItemEntity(context: context)
                entity.id = item.id
                entity.name = item.name
                entity.countrycode = item.countrycode
                entity.sourceUrl = item.sourceUrl
                entity.fileUrl = item.fileUrl
                entity.image = item.image
                entity.sizeBytes = item.sizeBytes
                entity.createdAt = item.createdAt
                
                do {
                    try context.save()
                    continuation.resume()
                } catch {
                    print("Error adding downloaded item: \(error)")
                    continuation.resume()
                }
            }
        }
    }
    
    func removeDownloadedItem(_ item: DownloadedItem) async {
        await withCheckedContinuation { continuation in
            coreDataManager.performBackgroundTask { context in
                let request: NSFetchRequest<DownloadedItemEntity> = DownloadedItemEntity.fetchRequest()
                request.predicate = NSPredicate(format: "id == %@", item.id)
                
                do {
                    let results = try context.fetch(request)
                    for entity in results {
                        context.delete(entity)
                    }
                    
                    try context.save()
                    continuation.resume()
                } catch {
                    print("Error removing downloaded item: \(error)")
                    continuation.resume()
                }
            }
        }
    }
    
    // MARK: - Recent Stations Operations
    func addRecentStation(_ station: Station) async {
        let prefs = SharedPreferencesRepository()
        prefs.addRecent(station.id)
    }
    
    func getRecentStations() async -> [Station] {
        let prefs = SharedPreferencesRepository()
        let recentIds = prefs.getRecents()
        
        guard !recentIds.isEmpty else { return [] }
        
        return await withCheckedContinuation { continuation in
            coreDataManager.performAsync { context in
                let request: NSFetchRequest<StationEntity> = StationEntity.fetchRequest()
                request.predicate = NSPredicate(format: "id IN %@", recentIds)
                
                do {
                    let results = try context.fetch(request)
                    let stations = results.map { $0.toStation() }
                    
                    // Sort by recents order
                    let sortedStations = recentIds.compactMap { id in
                        stations.first(where: { $0.id == id })
                    }
                    
                    continuation.resume(returning: sortedStations)
                } catch {
                    print("Error fetching recent stations: \(error)")
                    continuation.resume(returning: [])
                }
            }
        }
    }
    
    func clearRecentStations() async {
        let prefs = SharedPreferencesRepository()
        prefs.clearRecents()
    }
}

// MARK: - Shared Preferences Repository (UserDefaults)
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
    
    func clearRecents() {
        userDefaults.removeObject(forKey: "recents")
    }
}