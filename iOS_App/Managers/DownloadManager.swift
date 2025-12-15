import Foundation
import Combine

// MARK: - Download Manager
class DownloadManager: NSObject, ObservableObject {
    static let shared = DownloadManager()
    
    @Published var activeDownloads: [String: Double] = [:] // StationID -> Progress
    
    private var urlSession: URLSession!
    private let databaseRepository = CoreDataRepository()
    
    override init() {
        super.init()
        let config = URLSessionConfiguration.background(withIdentifier: "com.srivyaradio.downloads")
        config.isDiscretionary = false
        config.sessionSendsLaunchEvents = true
        urlSession = URLSession(configuration: config, delegate: self, delegateQueue: nil)
    }
    
    func startDownload(for station: Station) {
        guard let url = URL(string: station.url_resolved) else { return }
        
        let task = urlSession.downloadTask(with: url)
        task.taskDescription = station.id
        task.resume()
        
        DispatchQueue.main.async {
            self.activeDownloads[station.id] = 0.0
        }
    }
    
    func cancelDownload(for stationId: String) {
        urlSession.getAllTasks { tasks in
            tasks.first { $0.taskDescription == stationId }?.cancel()
        }
        
        DispatchQueue.main.async {
            self.activeDownloads.removeValue(forKey: stationId)
        }
    }
    
    func deleteDownload(_ item: DownloadedItem) {
        // Remove file
        if let url = URL(string: item.fileUrl) {
            try? FileManager.default.removeItem(at: url)
        }
        
        // Remove from database
        Task {
            await databaseRepository.removeDownloadedItem(item)
        }
    }
}

// MARK: - URLSessionDelegate
extension DownloadManager: URLSessionDownloadDelegate {
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        guard let stationId = downloadTask.taskDescription else { return }
        
        // Move file to Documents directory
        let fileManager = FileManager.default
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let destinationURL = documentsURL.appendingPathComponent("\(stationId).mp3") // Assumption: MP3
        
        do {
            if fileManager.fileExists(atPath: destinationURL.path) {
                try fileManager.removeItem(at: destinationURL)
            }
            try fileManager.moveItem(at: location, to: destinationURL)
            
            // Create DownloadedItem record
            Task {
                // We need station details. For now, fetch from repo or minimal data.
                // Since this is background, we might rely on the ID.
                if let station = await databaseRepository.getStation(by: stationId) {
                    let item = DownloadedItem(
                        id: stationId,
                        name: station.name,
                        countrycode: station.countrycode,
                        sourceUrl: station.url_resolved,
                        fileUrl: destinationURL.absoluteString,
                        image: station.favicon,
                        sizeBytes: try! Int64(fileManager.attributesOfItem(atPath: destinationURL.path)[.size] as? Int ?? 0),
                        createdAt: Date()
                    )
                    await databaseRepository.addDownloadedItem(item)
                }
            }
            
        } catch {
            print("File move failed: \(error)")
        }
        
        DispatchQueue.main.async {
            self.activeDownloads.removeValue(forKey: stationId)
        }
    }
    
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        guard let stationId = downloadTask.taskDescription, totalBytesExpectedToWrite > 0 else { return }
        
        let progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
        DispatchQueue.main.async {
            self.activeDownloads[stationId] = progress
        }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        if let error = error {
            print("Download error: \(error)")
        }
        
        if let stationId = task.taskDescription {
            DispatchQueue.main.async {
                self.activeDownloads.removeValue(forKey: stationId)
            }
        }
    }
}
