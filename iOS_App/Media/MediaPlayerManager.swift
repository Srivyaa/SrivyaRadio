import Foundation
import AVFoundation
import MediaPlayer
import Combine

// MARK: - Media Player Manager
class MediaPlayerManager: NSObject, ObservableObject {
    static let shared = MediaPlayerManager()
    
    @Published var isPlaying = false
    @Published var isLoading = false
    @Published var currentStation: Station?
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    @Published var isShuffleEnabled = false
    @Published var repeatMode: RepeatMode = .off
    @Published var queue: [Station] = []
    @Published var currentQueueIndex: Int = 0
    
    private let player = AVPlayer()
    private let audioSession = AVAudioSession.sharedInstance()
    private var timeObserver: Any?
    private var stationPlayerItems: [String: AVPlayerItem] = [:]
    
    override init() {
        super.init()
        setupAudioSession()
        setupRemoteCommandCenter()
        setupPlayerObservers()
        configurePlayer()
    }
    
    deinit {
        removeTimeObserver()
        NotificationCenter.default.removeObserver(self)
    }
    
    // MARK: - Setup Methods
    private func setupAudioSession() {
        do {
            try audioSession.setCategory(.playback, mode: .default, options: [.mixWithOthers, .allowBluetooth])
            try audioSession.setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }
    
    private func configurePlayer() {
        player.actionAtItemEnd = .none
    }
    
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // Play Command
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.play()
            return .success
        }
        
        // Pause Command
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.pause()
            return .success
        }
        
        // Next Track Command
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.skipToNext()
            return .success
        }
        
        // Previous Track Command
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.skipToPrevious()
            return .success
        }
        
        // Toggle Shuffle Command
        commandCenter.changeShuffleModeCommand.addTarget { [weak self] event in
            if let command = event as? MPChangeShuffleModeCommandEvent {
                self?.toggleShuffle(enabled: command.shuffleType == .items)
                return .success
            }
            return .commandFailed
        }
        
        // Toggle Repeat Command
        commandCenter.changeRepeatModeCommand.addTarget { [weak self] event in
            if let command = event as? MPChangeRepeatModeCommandEvent {
                let repeatMode: RepeatMode
                switch command.repeatType {
                case .off:
                    repeatMode = .off
                case .one:
                    repeatMode = .one
                case .all:
                    repeatMode = .all
                @unknown default:
                    repeatMode = .off
                }
                self?.setRepeatMode(repeatMode)
                return .success
            }
            return .commandFailed
        }
        
        // Seek Forward Command
        commandCenter.seekForwardCommand.addTarget { [weak self] _ in
            self?.seekForward()
            return .success
        }
        
        // Seek Backward Command
        commandCenter.seekBackwardCommand.addTarget { [weak self] _ in
            self?.seekBackward()
            return .success
        }
    }
    
    private func setupPlayerObservers() {
        // Time observer for progress updates
        timeObserver = player.addPeriodicTimeObserver(forInterval: CMTime(seconds: 1, preferredTimescale: 1), queue: .main) { [weak self] time in
            self?.currentTime = time.seconds
        }
        
        // Player status observer
        player.addObserver(self, forKeyPath: "status", options: [.new, .old], context: nil)
        player.addObserver(self, forKeyPath: "currentItem", options: [.new, .old], context: nil)
        
        // Notification center for player item end
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(playerItemDidReachEnd(_:)),
            name: .AVPlayerItemDidPlayToEndTime,
            object: player.currentItem
        )
    }
    
    // MARK: - Player Controls
    func playStation(_ station: Station) {
        currentStation = station
        isLoading = true
        
        // Check for interstitial ad
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = scene.windows.first?.rootViewController {
            AdMobManager.shared.incrementPlayCountAndCheck(rootViewController: rootVC)
        }
        
        // Add to recent stations
        // recentManager.addStation(station)
        
        let playerItem = getPlayerItem(for: station)
        player.replaceCurrentItem(with: playerItem)
        player.play()
        
        updateNowPlayingInfo()
        updateRemoteCommandCenter()
    }
    
    func play() {
        player.play()
        isPlaying = true
        updateRemoteCommandCenter()
    }
    
    func pause() {
        player.pause()
        isPlaying = false
        updateRemoteCommandCenter()
    }
    
    func togglePlayPause() {
        if isPlaying {
            pause()
        } else {
            play()
        }
    }
    
    func stop() {
        player.pause()
        player.seek(to: CMTime.zero)
        isPlaying = false
    }
    
    // MARK: - Queue Management
    func setQueue(_ stations: [Station], startingIndex: Int = 0) {
        queue = stations
        currentQueueIndex = startingIndex
        
        if let station = stations[safe: startingIndex] {
            playStation(station)
        }
    }
    
    func skipToNext() {
        guard !queue.isEmpty else { return }
        
        switch repeatMode {
        case .one:
            // Repeat current song
            player.seek(to: CMTime.zero)
            player.play()
        case .all:
            currentQueueIndex = (currentQueueIndex + 1) % queue.count
            if let station = queue[safe: currentQueueIndex] {
                playStation(station)
            }
        case .off:
            if currentQueueIndex < queue.count - 1 {
                currentQueueIndex += 1
                if let station = queue[safe: currentQueueIndex] {
                    playStation(station)
                }
            } else {
                pause()
            }
        }
    }
    
    func skipToPrevious() {
        guard !queue.isEmpty else { return }
        
        switch repeatMode {
        case .one, .all:
            if currentTime > 3 {
                // Seek to beginning if more than 3 seconds into the song
                player.seek(to: CMTime.zero)
                return
            }
        case .off:
            break
        }
        
        if currentQueueIndex > 0 {
            currentQueueIndex -= 1
            if let station = queue[safe: currentQueueIndex] {
                playStation(station)
            }
        }
    }
    
    // MARK: - Shuffle and Repeat
    func toggleShuffle(enabled: Bool? = nil) {
        let newEnabled = enabled ?? !isShuffleEnabled
        isShuffleEnabled = newEnabled
        
        // Shuffle the queue
        if newEnabled && !queue.isEmpty {
            queue.shuffle()
            currentQueueIndex = queue.firstIndex { $0.id == currentStation?.id } ?? 0
        }
        
        updateRemoteCommandCenter()
    }
    
    enum RepeatMode {
        case off, one, all
    }
    
    func setRepeatMode(_ mode: RepeatMode) {
        repeatMode = mode
        updateRemoteCommandCenter()
    }
    
    // MARK: - Seek Controls
    func seek(to time: TimeInterval) {
        player.seek(to: CMTime(seconds: time, preferredTimescale: 1))
    }
    
    func seekForward() {
        let newTime = currentTime + 10
        seek(to: newTime)
    }
    
    func seekBackward() {
        let newTime = max(0, currentTime - 10)
        seek(to: newTime)
    }
    
    // MARK: - Sleep Timer
    func setSleepTimer(minutes: Int) {
        let totalSeconds = minutes * 60
        
        DispatchQueue.main.asyncAfter(deadline: .now() + TimeInterval(totalSeconds)) { [weak self] in
            self?.pause()
        }
    }
    
    // MARK: - Private Methods
    private func getPlayerItem(for station: Station) -> AVPlayerItem {
        if let existingItem = stationPlayerItems[station.id] {
            return existingItem
        }
        
        guard let url = URL(string: station.url_resolved) else {
            fatalError("Invalid URL for station: \(station.name)")
        }
        
        let playerItem = AVPlayerItem(url: url)
        playerItem.asset.loadValuesAsynchronously(forKeys: ["playable"]) { [weak self] in
            DispatchQueue.main.async {
                self?.isLoading = false
            }
        }
        
        stationPlayerItems[station.id] = playerItem
        return playerItem
    }
    
    private func updateNowPlayingInfo() {
        guard let station = currentStation else { return }
        
        var nowPlayingInfo = [String: Any]()
        nowPlayingInfo[MPMediaItemPropertyTitle] = station.name
        nowPlayingInfo[MPMediaItemPropertyArtist] = station.country
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = "SrivyaRadio"
        
        if let url = URL(string: station.favicon), !station.favicon.isEmpty {
            // Load artwork asynchronously
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let artwork = UIImage(data: data) {
                    let image = artwork.resize(to: CGSize(width: 300, height: 300))
                    nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image.size) { _ in
                        return image
                    }
                    
                    MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
                }
            }.resume()
        }
        
        nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = true
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    private func updateRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // Update play/pause button state
        commandCenter.playCommand.isEnabled = !isPlaying
        commandCenter.pauseCommand.isEnabled = isPlaying
        
        // Update shuffle state
        commandCenter.changeShuffleModeCommand.isEnabled = true
        let shuffleType: MPShuffleType = isShuffleEnabled ? .items : .off
        commandCenter.changeShuffleModeCommand.currentShuffleType = shuffleType
        
        // Update repeat state
        commandCenter.changeRepeatModeCommand.isEnabled = true
        let repeatType: MPRepeatType
        switch repeatMode {
        case .off:
            repeatType = .off
        case .one:
            repeatType = .one
        case .all:
            repeatType = .all
        }
        commandCenter.changeRepeatModeCommand.currentRepeatType = repeatType
        
        // Update next/previous button states
        commandCenter.nextTrackCommand.isEnabled = !queue.isEmpty
        commandCenter.previousTrackCommand.isEnabled = !queue.isEmpty
    }
    
    private func removeTimeObserver() {
        if let observer = timeObserver {
            player.removeTimeObserver(observer)
            timeObserver = nil
        }
    }
    
    // MARK: - Player Observers
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "status" {
            DispatchQueue.main.async { [weak self] in
                switch self?.player.status {
                case .readyToPlay:
                    self?.isLoading = false
                case .failed:
                    self?.isLoading = false
                    print("Player failed: \(self?.player.error?.localizedDescription ?? "Unknown error")")
                case .unknown:
                    break
                case .none:
                    break
                @unknown default:
                    break
                }
            }
        }
    }
    
    @objc private func playerItemDidReachEnd(_ notification: Notification) {
        // Auto-advance to next track
        if repeatMode != .one {
            skipToNext()
        } else {
            // Loop current track
            player.seek(to: CMTime.zero)
            player.play()
        }
    }
}

// MARK: - Extensions
extension Array {
    subscript(safe index: Index) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}

extension UIImage {
    func resize(to size: CGSize) -> UIImage {
        UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
        self.draw(in: CGRect(origin: .zero, size: size))
        let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resizedImage ?? self
    }
}