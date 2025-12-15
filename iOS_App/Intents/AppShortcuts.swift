import AppIntents
import SwiftUI

// Only available on iOS 16+
@available(iOS 16.0, *)
struct PlayStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Play Station"
    static var description = IntentDescription("Plays a specific station in Srivya Radio")
    
    @Parameter(title: "Station Name")
    var stationName: String
    
    func perform() async throws -> some IntentResult {
        // Logic to find and play station
        // For simplicity in this intent, we might search by name
        let repo = CoreDataRepository()
        let stations = await repo.searchStations(query: stationName)
        
        if let station = stations.first {
            await MainActor.run {
                MediaPlayerManager.shared.playStation(station)
            }
            return .result(dialog: "Playing \(station.name)")
        } else {
            return .result(dialog: "Station not found")
        }
    }
}

@available(iOS 16.0, *)
struct SrivyaRadioShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: PlayStationIntent(),
            phrases: [
                "Play \(.applicationName)",
                "Play station in \(.applicationName)"
            ],
            shortTitle: "Play Station",
            systemImageName: "play.circle.fill"
        )
    }
}
