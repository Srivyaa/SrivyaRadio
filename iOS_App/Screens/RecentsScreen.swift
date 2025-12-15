import SwiftUI

// MARK: - Recents View Model
class RecentsViewModel: ObservableObject {
    @Published var stations: [Station] = []
    
    private let databaseRepository: DatabaseRepositoryProtocol
    
    init(databaseRepository: DatabaseRepositoryProtocol = CoreDataRepository()) {
        self.databaseRepository = databaseRepository
    }
    
    func loadRecents() {
        Task {
            let recentStations = await databaseRepository.getRecentStations()
            await MainActor.run {
                self.stations = recentStations
            }
        }
    }
    
    func clearRecents() {
        Task {
            await databaseRepository.clearRecentStations()
            await loadRecents()
        }
    }
}

// MARK: - Recents Screen
struct RecentsScreen: View {
    @EnvironmentObject var mediaPlayerManager: MediaPlayerManager
    @StateObject private var viewModel = RecentsViewModel()
    
    var body: some View {
        List {
            if viewModel.stations.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "clock")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("No Recently Played Stations")
                        .font(.headline)
                    Text("Stations you play will appear here")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .listRowBackground(Color.clear)
            } else {
                ForEach(viewModel.stations) { station in
                    StationRow(station: station)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            mediaPlayerManager.playStation(station)
                        }
                }
            }
        }
        .navigationTitle("Recents")
        .onAppear {
            viewModel.loadRecents()
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                if !viewModel.stations.isEmpty {
                    Button(action: {
                        viewModel.clearRecents()
                    }) {
                        Image(systemName: "trash")
                            .foregroundColor(.red)
                    }
                }
            }
        }
    }
}
