import Foundation
import SwiftUI
import Combine

class DeepLinkManager: ObservableObject {
    static let shared = DeepLinkManager()
    
    @Published var selectedTab: Int = 0
    @Published var activeStationId: String?
    
    // URL Scheme: srivyaradio://
    // Routes:
    // - home
    // - favorites
    // - play?id={station_id}
    
    func handle(url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else { return }
        
        print("Handling Deep Link: \(url.absoluteString)")
        print("Host: \(components.host ?? "nil")")
        
        switch components.host {
        case "home":
            selectedTab = 0
            
        case "favorites":
            selectedTab = 1
            
        case "play":
            // Go to home (discover) or remain current, but trigger play
            selectedTab = 0 
            if let queryItems = components.queryItems,
               let stationId = queryItems.first(where: { $0.name == "id" })?.value {
                playStation(id: stationId)
            }
            
        default:
            break
        }
    }
    
    private func playStation(id: String) {
        // Find station and play
        // Since we need access to data, we can use the Repository
        Task {
            if let station = await CoreDataRepository().getStation(by: id) {
                await MainActor.run {
                    MediaPlayerManager.shared.playStation(station)
                }
            } else {
                print("Station not found for deep link ID: \(id)")
            }
        }
    }
}
