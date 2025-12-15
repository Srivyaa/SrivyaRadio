import Foundation

// MARK: - Stations Client Protocol
protocol StationsClientProtocol {
    func getStations(for country: String) async throws -> [Station]
    func getVersion() async throws -> String
}

// MARK: - Stations Client Implementation
class StationsClient: StationsClientProtocol {
    static let shared = StationsClient()
    private let session: URLSession
    private let baseURL = "https://srivyaa.github.io/RadioStations/"
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 60
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        self.session = URLSession(configuration: config)
    }
    
    // MARK: - Get Stations for Country
    func getStations(for country: String) async throws -> [Station] {
        let countryCode = country.uppercased()
        let urlString = "\(baseURL)data/\(countryCode).json"
        
        guard let url = URL(string: urlString) else {
            throw NetworkError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        do {
            let decoder = JSONDecoder()
            let stations = try decoder.decode([Station].self, from: data)
            return stations
        } catch {
            throw NetworkError.decodingError(error)
        }
    }
    
    // MARK: - Get Version
    func getVersion() async throws -> String {
        let urlString = "\(baseURL)version.txt"
        
        guard let url = URL(string: urlString) else {
            throw NetworkError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        return String(data: data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }
}

// MARK: - Network Errors
enum NetworkError: LocalizedError {
    case invalidURL
    case invalidResponse
    case serverError(Int)
    case decodingError(Error)
    case noData
    case networkError(Error)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .invalidResponse:
            return "Invalid response from server"
        case .serverError(let code):
            return "Server error with status code: \(code)"
        case .decodingError(let error):
            return "Failed to decode response: \(error.localizedDescription)"
        case .noData:
            return "No data received from server"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        }
    }
}

// MARK: - Location Client
class LocationClient: ObservableObject {
    static let shared = LocationClient()
    private let session: URLSession
    private let baseURL = "http://ip-api.com/"
    
    private init() {
        self.session = URLSession.shared
    }
    
    func getCountryCode() async throws -> String {
        let urlString = "\(baseURL)json"
        
        guard let url = URL(string: urlString) else {
            throw NetworkError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        let decoder = JSONDecoder()
        let locationData = try decoder.decode(LocationResponse.self, from: data)
        
        return locationData.countryCode
    }
}

// MARK: - Location Response Model
struct LocationResponse: Codable {
    let countryCode: String
    
    enum CodingKeys: String, CodingKey {
        case countryCode = "countryCode"
    }
}