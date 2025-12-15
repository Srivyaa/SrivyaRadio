import SwiftUI

// MARK: - Favorites View Model
class FavoritesViewModel: ObservableObject {
    @Published var favorites: [Station] = []
    @Published var isLoading = false
    @Published var editMode: EditMode = .inactive
    
    private let databaseRepository: DatabaseRepositoryProtocol
    
    init(databaseRepository: DatabaseRepositoryProtocol = CoreDataRepository()) {
        self.databaseRepository = databaseRepository
        loadFavorites()
    }
    
    func loadFavorites() {
        Task {
            let favoriteStations = await databaseRepository.getFavorites()
            await MainActor.run {
                self.favorites = favoriteStations
            }
        }
    }
    
    func addToFavorites(_ station: Station) {
        Task {
            await databaseRepository.addToFavorites(station)
            loadFavorites()
        }
    }
    
    func removeFromFavorites(_ station: Station) {
        Task {
            await databaseRepository.removeFromFavorites(station)
            loadFavorites()
        }
    }
    
    func moveFavorites(from source: IndexSet, to destination: Int) {
        favorites.move(fromOffsets: source, toOffset: destination)
        // Note: In a real implementation, you'd update the order in Core Data here
    }
    
    func deleteFavorites(at offsets: IndexSet) {
        for index in offsets {
            let station = favorites[index]
            removeFromFavorites(station)
        }
    }
    
    func toggleEditMode() {
        editMode = editMode == .active ? .inactive : .active
    }
    
    func addToRecents(_ station: Station) async {
        await databaseRepository.addRecentStation(station)
    }
}

// MARK: - Favorites Screen
struct FavoritesScreen: View {
    @EnvironmentObject var mediaPlayerManager: MediaPlayerManager
    @StateObject private var viewModel = FavoritesViewModel()
    
    var body: some View {
        List {
            if viewModel.favorites.isEmpty {
                // Empty state
                VStack(spacing: 20) {
                    Image(systemName: "heart")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    
                    Text("No Favorites Yet")
                        .font(.title2)
                        .fontWeight(.medium)
                    
                    Text("Add stations to your favorites by tapping the heart icon while browsing.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .listRowBackground(Color.clear)
            } else {
                ForEach(viewModel.favorites) { station in
                    StationRow(station: station)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            // Set queue and play
                            if let index = viewModel.favorites.firstIndex(where: { $0.id == station.id }) {
                                mediaPlayerManager.setQueue(viewModel.favorites, startingIndex: index)
                            } else {
                                mediaPlayerManager.playStation(station)
                            }
                            
                            // Add to recents
                            Task {
                                await viewModel.addToRecents(station)
                            }
                        }
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            // Remove from favorites
                            Button(role: .destructive) {
                                viewModel.removeFromFavorites(station)
                            } label: {
                                Label("Remove", systemImage: "heart.slash")
                            }
                        }
                }
                .onMove(of: \.self) { from, to in
                    viewModel.moveFavorites(from: from, to: to)
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Favorites")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.toggleEditMode()
                }) {
                    Text(editMode == .active ? "Done" : "Edit")
                }
                .disabled(viewModel.favorites.isEmpty)
            }
        }
        .environment(\.editMode, $viewModel.editMode)
        .refreshable {
            viewModel.loadFavorites()
        }
        .onAppear {
            viewModel.loadFavorites()
        }
    }
    
    private var editMode: EditMode {
        get { viewModel.editMode }
        set { viewModel.editMode = newValue }
    }
}

// MARK: - Enhanced Station Row for Favorites
struct FavoriteStationRow: View {
    let station: Station
    let onRemove: () -> Void
    let onPlay: () -> Void
    
    @State private var showingOptions = false
    
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
            .frame(width: 60, height: 60)
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
            
            // Play button
            Button(action: onPlay) {
                Image(systemName: "play.circle.fill")
                    .foregroundColor(.blue)
                    .font(.title2)
            }
            .buttonStyle(PlainButtonStyle())
            
            // Options button
            Button(action: { showingOptions = true }) {
                Image(systemName: "ellipsis.circle")
                    .foregroundColor(.secondary)
                    .font(.title3)
            }
            .buttonStyle(PlainButtonStyle())
            .confirmationDialog("Station Options", isPresented: $showingOptions) {
                Button("Play Now") { onPlay() }
                Button("Remove from Favorites", role: .destructive) { onRemove() }
                Button("Cancel", role: .cancel") { }
            }
        }
        .padding(.vertical, 4)
    }
}