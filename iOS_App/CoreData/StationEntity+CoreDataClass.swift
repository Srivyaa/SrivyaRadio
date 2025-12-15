import Foundation
import CoreData

@objc(StationEntity)
public class StationEntity: NSManagedObject {
    
    // MARK: - Station Conversion
    func toStation() -> Station {
        Station(
            id: id ?? "",
            favicon: favicon ?? "",
            name: name ?? "",
            country: country ?? "",
            tags: tags ?? "",
            countrycode: countrycode ?? "",
            url_resolved: url_resolved ?? "",
            state: state ?? "",
            homepage: homepage ?? "",
            rank: Int(rank)
        )
    }
}

// MARK: - Station Struct
struct Station: Codable, Identifiable, Hashable {
    let id: String
    var favicon: String
    var name: String
    var country: String
    var tags: String
    var countrycode: String
    var url_resolved: String
    var state: String
    var homepage: String
    var rank: Int
    
    enum CodingKeys: String, CodingKey {
        case id
        case favicon
        case name
        case country
        case tags
        case countrycode
        case url_resolved
        case state
        case homepage
        case rank
    }
}

// MARK: - Station Entity Extension
extension StationEntity {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<StationEntity> {
        return NSFetchRequest<StationEntity>(entityName: "StationEntity")
    }
    
    @NSManaged public var id: String?
    @NSManaged public var favicon: String?
    @NSManaged public var name: String?
    @NSManaged public var country: String?
    @NSManaged public var tags: String?
    @NSManaged public var countrycode: String?
    @NSManaged public var url_resolved: String?
    @NSManaged public var state: String?
    @NSManaged public var homepage: String?
    @NSManaged public var rank: Int32
    @NSManaged public var createdAt: Date?
}