import Foundation
import CoreData

@objc(DownloadedItemEntity)
public class DownloadedItemEntity: NSManagedObject {
    
    // MARK: - DownloadedItem Conversion
    func toDownloadedItem() -> DownloadedItem {
        DownloadedItem(
            id: id ?? "",
            name: name ?? "",
            countrycode: countrycode ?? "",
            sourceUrl: sourceUrl ?? "",
            fileUrl: fileUrl ?? "",
            image: image ?? "",
            sizeBytes: sizeBytes,
            createdAt: createdAt ?? Date()
        )
    }
}

// MARK: - DownloadedItem Struct
struct DownloadedItem: Codable, Identifiable, Hashable {
    let id: String
    var name: String
    var countrycode: String
    var sourceUrl: String
    var fileUrl: String
    var image: String
    var sizeBytes: Int64
    var createdAt: Date
}

// MARK: - DownloadedItem Entity Extension
extension DownloadedItemEntity {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<DownloadedItemEntity> {
        return NSFetchRequest<DownloadedItemEntity>(entityName: "DownloadedItemEntity")
    }
    
    @NSManaged public var id: String?
    @NSManaged public var name: String?
    @NSManaged public var countrycode: String?
    @NSManaged public var sourceUrl: String?
    @NSManaged public var fileUrl: String?
    @NSManaged public var image: String?
    @NSManaged public var sizeBytes: Int64
    @NSManaged public var createdAt: Date?
}