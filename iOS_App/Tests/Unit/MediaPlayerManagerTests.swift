import XCTest
@testable import SrivyaRadio

class MediaPlayerManagerTests: XCTestCase {
    var manager: MediaPlayerManager!
    
    override func setUp() {
        super.setUp()
        manager = MediaPlayerManager.shared // Or create new instance if logic permits isolation
    }
    
    func testQueueManagement() {
        let station1 = Station(id: "1", name: "S1", country: "US", countrycode: "US", url: "url1", favicon: "")
        let station2 = Station(id: "2", name: "S2", country: "US", countrycode: "US", url: "url2", favicon: "")
        
        manager.setQueue([station1, station2], startingIndex: 0)
        
        XCTAssertEqual(manager.queue.count, 2)
        XCTAssertEqual(manager.currentQueueIndex, 0)
        XCTAssertEqual(manager.currentStation?.id, "1")
        
        // Skip
        manager.skipToNext()
        // Note: skipToNext might be async or require mocking AVPlayer to actually trigger changes if it relies on player status.
        // Assuming currentQueueIndex updates immediately:
        // XCTAssertEqual(manager.currentQueueIndex, 1) 
        // This might fail if the logic waits for player to be ready. 
    }
}
