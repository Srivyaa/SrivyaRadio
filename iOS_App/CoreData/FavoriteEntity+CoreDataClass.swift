import Foundation
import CoreData

@objc(FavoriteEntity)
public class FavoriteEntity: NSManagedObject {
    
    // MARK: - Favorite Conversion
    func toFavorite() -> Favorite {
        Favorite(
            id: id ?? "",
            order: Int(order),
            stationId: stationId ?? ""
        )
    }
}

// MARK: - Favorite Struct
struct Favorite: Codable, Identifiable, Hashable {
    let id: String
    var order: Int
    var stationId: String
}

// MARK: - Favorite Entity Extension
extension FavoriteEntity {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<FavoriteEntity> {
        return NSFetchRequest<FavoriteEntity>(entityName: "FavoriteEntity")
    }
    
    @NSManaged public var id: String?
    @NSManaged public var order: Int32
    @NSManaged public var stationId: String?
    @NSManaged public var createdAt: Date?
}