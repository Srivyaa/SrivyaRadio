import SwiftUI

// MARK: - Queue Screen
struct QueueScreen: View {
    @EnvironmentObject var mediaPlayerManager: MediaPlayerManager
    
    var body: some View {
        List {
            if mediaPlayerManager.queue.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("Queue is Empty")
                        .font(.headline)
                    Text("Add stations to queue to see them here")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .listRowBackground(Color.clear)
            } else {
                ForEach(Array(mediaPlayerManager.queue.enumerated()), id: \.offset) { index, station in
                    HStack {
                        if index == mediaPlayerManager.currentQueueIndex {
                            Image(systemName: "speaker.wave.3.fill")
                                .foregroundColor(.blue)
                                .padding(.trailing, 8)
                        } else {
                            Text("\(index + 1)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .frame(width: 24)
                                .padding(.trailing, 8)
                        }
                        
                        VStack(alignment: .leading) {
                            Text(station.name)
                                .font(.headline)
                            Text(station.country)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        
                        Spacer()
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        mediaPlayerManager.setQueue(mediaPlayerManager.queue, startingIndex: index)
                    }
                }
            }
        }
        .navigationTitle("Now Playing Queue")
    }
}
