import XCTest

class MainFlowUITests: XCTestCase {
    let app = XCUIApplication()
    
    override func setUp() {
        continueAfterFailure = false
        app.launch()
    }
    
    func testTabNavigation() {
        // Check if tab bar exists
        XCTAssertTrue(app.tabBars.firstMatch.exists)
        
        // Navigate to Favorites
        app.tabBars.buttons["Favorites"].tap()
        XCTAssertTrue(app.navigationBars["Favorites"].exists)
        
        // Navigate to More
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.navigationBars["More"].exists)
    }
    
    func testDiscoverScreenLoads() {
        app.tabBars.buttons["Discover"].tap()
        // Wait for loading?
        // Check for specific elements
    }
}
