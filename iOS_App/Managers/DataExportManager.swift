import Foundation
import CoreData
import UIKit

class DataExportManager {
    static let shared = DataExportManager()
    private let repository = CoreDataRepository()
    
    // MARK: - Export
    func exportFavoritesToCSV() async throws -> URL {
        let favorites = await repository.getFavorites()
        
        var csvString = "id,name,country,countrycode,url,favicon\n"
        
        for station in favorites {
            let row = [
                escapeCSV(station.id),
                escapeCSV(station.name),
                escapeCSV(station.country),
                escapeCSV(station.countrycode),
                escapeCSV(station.url),
                escapeCSV(station.favicon)
            ].joined(separator: ",")
            
            csvString.append(row + "\n")
        }
        
        let fileName = "SrivyaRadio_Favorites_\(Date().timeIntervalSince1970).csv"
        let tempUrl = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        
        try csvString.write(to: tempUrl, atomically: true, encoding: .utf8)
        return tempUrl
    }
    
    private func escapeCSV(_ text: String) -> String {
        if text.contains(",") || text.contains("\"") || text.contains("\n") {
            let escaped = text.replacingOccurrences(of: "\"", with: "\"\"")
            return "\"\(escaped)\""
        }
        return text
    }
    
    // MARK: - Import
    func importFavoritesFromCSV(url: URL) async throws -> Int {
        // Start accessing security scoped resource if needed (for file picker URLs)
        let accessing = url.startAccessingSecurityScopedResource()
        defer {
            if accessing {
                url.stopAccessingSecurityScopedResource()
            }
        }
        
        let data = try Data(contentsOf: url)
        guard let content = String(data: data, encoding: .utf8) else {
            throw NSError(domain: "DataExportManager", code: 1, userInfo: [NSLocalizedDescriptionKey: "Invalid file encoding"])
        }
        
        let rows = content.components(separatedBy: "\n")
        var importedCount = 0
        
        // Skip header
        for (index, row) in rows.enumerated() {
            if index == 0 || row.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty { continue }
            
            let columns = parseCSVRow(row)
            if columns.count >= 5 {
                // Determine layout based on header or assume standard: id,name,country,countrycode,url,favicon
                // We'll generate a new UUID if ID collision or specific logic? 
                // Repository uses ID as unique constraint usually.
                
                // If columns[0] is original ID, we might keep it to avoid dupes if importing same list
                
                let station = Station(
                    changeuuid: UUID().uuidString, // Assign new UUID or use columns[0] if valid? Let's keep original ID if possible but 'id' in our Station struct is let.
                    // Actually Station struct maps 'stationuuid' usually.
                    // Let's create a new Station object.
                    stationuuid: columns[0].trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: "\"", with: ""),
                    name: columns[1],
                    url: columns[4],
                    url_resolved: columns[4], // Assume same
                    homepage: "", 
                    tags: "",
                    country: columns[2],
                    countrycode: columns[3],
                    state: "",
                    language: "",
                    votes: 0,
                    codec: "",
                    favicon: columns.count > 5 ? columns[5] : ""
                )
                
                await repository.addToFavorites(station)
                importedCount += 1
            }
        }
        
        return importedCount
    }
    
    private func parseCSVRow(_ row: String) -> [String] {
        var result: [String] = []
        var current = ""
        var insideQuotes = false
        
        for char in row {
            if char == "\"" {
                insideQuotes.toggle()
            } else if char == "," && !insideQuotes {
                result.append(current)
                current = ""
            } else {
                current.append(char)
            }
        }
        result.append(current)
        
        // Clean quotes from results
        return result.map { field in
            if field.hasPrefix("\"") && field.hasSuffix("\"") && field.count >= 2 {
                return String(field.dropFirst().dropLast()).replacingOccurrences(of: "\"\"", with: "\"")
            }
            return field
        }
    }
}
