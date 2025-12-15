import XCTest
import Combine
@testable import SrivyaRadio

class DiscoverViewModelTests: XCTestCase {
    var viewModel: DiscoverViewModel!
    var mockRepository: MockDatabaseRepository!
    var cancellables: Set<AnyCancellable>!
    
    override func setUp() {
        super.setUp()
        mockRepository = MockDatabaseRepository()
        viewModel = DiscoverViewModel(repository: mockRepository)
        cancellables = []
        
        // Seed mock data
        let testStation = Station(id: "1", name: "Test Radio", country: "US", countrycode: "US", url: "http://test.com", favicon: "")
        Task {
            await mockRepository.saveStations([testStation], countryCode: "US")
        }
    }
    
    override func tearDown() {
        viewModel = nil
        mockRepository = nil
        cancellables = nil
        super.tearDown()
    }
    
    func testFetchStations() {
        let expectation = XCTestExpectation(description: "Fetch stations")
        
        // Assume viewModel has a method or property to trigger fetch or observe
        // For simplicity, let's call the load method if exposed
        Task {
            await viewModel.loadStations(country: "US")
            
            // Check published property
            XCTAssertEqual(viewModel.stations.count, 1)
            XCTAssertEqual(viewModel.stations.first?.name, "Test Radio")
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: 2.0)
    }
    
    func testSearchStations() {
        // Setup scenarios...
    }
}
