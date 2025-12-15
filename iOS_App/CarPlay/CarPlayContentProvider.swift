import Foundation
import CarPlay

class CarPlayContentProvider {
    static let shared = CarPlayContentProvider()
    
    // Convert Station to CPListItem
    func listItem(for station: Station) -> CPListItem {
        let item = CPListItem(text: station.name, detailText: station.country)
        item.userInfo = station // Store station object for retrieval
        
        // Load image if available (async loading is tricky in CarPlay lists, usually we use a placeholder or cache)
        // For now, use a system image or synchronous check if cached
        item.setImage(UIImage(systemName: "radio"))
        
        return item
    }
    
    // Create tabs for the root template
    func createTabBarTemplate() -> CPTabBarTemplate {
        let favoritesTemplate = createFavoritesTemplate()
        let recentsTemplate = createRecentsTemplate()
        // let browseTemplate = createBrowseTemplate()
        
        return CPTabBarTemplate(templates: [favoritesTemplate, recentsTemplate])
    }
    
    private func createFavoritesTemplate() -> CPListTemplate {
        let template = CPListTemplate(title: "Favorites", sections: [])
        template.tabImage = UIImage(systemName: "heart.fill")
        return template
    }
    
    private func createRecentsTemplate() -> CPListTemplate {
        let template = CPListTemplate(title: "Recents", sections: [])
        template.tabImage = UIImage(systemName: "clock.fill")
        return template
    }
}
