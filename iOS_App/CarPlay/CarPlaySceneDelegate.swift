import CarPlay
import UIKit

class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {
    var interfaceController: CPInterfaceController?
    var window: CPWindow?
    
    private let contentProvider = CarPlayContentProvider.shared
    private let mediaPlayerManager = MediaPlayerManager.shared
    
    // MARK: - CPTemplateApplicationSceneDelegate
    
    func templateApplicationScene(_ templateApplicationScene: CPTemplateApplicationScene, didConnect interfaceController: CPInterfaceController, to window: CPWindow) {
        self.interfaceController = interfaceController
        self.window = window
        
        // Create root template (Tab Bar)
        let tabBarTemplate = contentProvider.createTabBarTemplate()
        
        // Set update handlers for tabs (to load data)
        tabBarTemplate.templates.forEach { template in
            if let listTemplate = template as? CPListTemplate {
                // Initial empty state or loading
                updateListTemplate(listTemplate)
            }
        }
        
        // Handle tab selection
        tabBarTemplate.updateHandler = { [weak self] _, selectedTemplate in
            if let listTemplate = selectedTemplate as? CPListTemplate {
                self?.updateListTemplate(listTemplate)
            }
        }
        
        // Set root
        interfaceController.setRootTemplate(tabBarTemplate, animated: true, completion: nil)
    }
    
    func templateApplicationScene(_ templateApplicationScene: CPTemplateApplicationScene, didDisconnectInterfaceController interfaceController: CPInterfaceController) {
        self.interfaceController = nil
        self.window = nil
    }
    
    // MARK: - Data Loading
    
    private func updateListTemplate(_ listTemplate: CPListTemplate) {
        Task {
            // Determine which tab this is by title
            let title = listTemplate.title?.lowercased() ?? ""
            var items: [CPListItem] = []
            
            if title.contains("favorites") {
                 let favorites = await CoreDataRepository().getFavorites()
                 items = favorites.map { contentProvider.listItem(for: $0) }
            } else if title.contains("recents") {
                 let recents = await CoreDataRepository().getRecentStations()
                 items = recents.map { contentProvider.listItem(for: $0) }
            }
            
            // Set handler for items
            items.forEach { item in
                item.handler = { [weak self] item, completion in
                    guard let station = item.userInfo as? Station else {
                        completion()
                        return
                    }
                    self?.playStation(station)
                    // Push Now Playing or handled by system
                    self?.interfaceController?.pushTemplate(CPNowPlayingTemplate.shared, animated: true, completion: nil)
                    completion()
                }
            }
            
            let section = CPListSection(items: items)
            listTemplate.updateSections([section])
        }
    }
    
    private func playStation(_ station: Station) {
        mediaPlayerManager.playStation(station)
    }
}
