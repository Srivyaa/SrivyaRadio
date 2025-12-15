import Foundation
import BackgroundTasks

// MARK: - Background Task Manager
class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    
    private let refreshTaskIdentifier = "com.srivyaradio.refresh"
    private let stationsClient = StationsClient.shared
    private let databaseRepository = CoreDataRepository()
    
    // MARK: - Registration
    func registerBackgroundTasks() {
        // Register App Refresh Task
        BGTaskScheduler.shared.register(forTaskWithIdentifier: refreshTaskIdentifier, using: nil) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }
    }
    
    // MARK: - Scheduling
    func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: refreshTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60) // 15 minutes minimum
        
        do {
            try BGTaskScheduler.shared.submit(request)
            // print("Background refresh scheduled")
        } catch {
            print("Could not schedule app refresh: \(error)")
        }
    }
    
    // MARK: - Handling
    private func handleAppRefresh(task: BGAppRefreshTask) {
        // Schedule the next refresh
        scheduleAppRefresh()
        
        // Create an operation that performs the main part of the background task
        let operation = Task {
            do {
                // Example: Refresh stations for the user's selected country
                // For now, we'll just log or do a lightweight check
                // In a real app, you might sync favorites or check for station updates
                
                // let country = UserDefaults.standard.string(forKey: "userCountry") ?? "US"
                // let stations = try await stationsClient.getStations(for: country)
                // await databaseRepository.saveStations(stations, countryCode: country)
                
                task.setTaskCompleted(success: true)
            } catch {
                print("Background refresh failed: \(error)")
                task.setTaskCompleted(success: false)
            }
        }
        
        // Provide an expiration handler for the background task
        task.expirationHandler = {
            operation.cancel()
        }
    }
}
