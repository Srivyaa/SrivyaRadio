import Foundation
import RevenueCat
import Combine

// MARK: - Purchase Manager
class PurchaseManager: ObservableObject {
    @Published var isPremium = false
    @Published var offerings: Offerings?
    @Published var customerInfo: CustomerInfo?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let revenueCatAPIKey = "your_revenuecat_api_key_here" // Replace with actual key
    
    init() {
        configureRevenueCat()
        checkEntitlements()
        loadOfferings()
    }
    
    private func configureRevenueCat() {
        Purchases.logLevel = .debug
        Purchases.configure(withAPIKey: revenueCatAPIKey)
        
        // Set up delegate to receive updates
        Purchases.delegate = self
    }
    
    private func checkEntitlements() {
        isLoading = true
        
        Purchases.shared.getCustomerInfo { [weak self] customerInfo, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    print("Error fetching customer info: \(error)")
                    return
                }
                
                self?.customerInfo = customerInfo
                self?.isPremium = customerInfo?.entitlements["premium"]?.isActive == true
                
                print("Premium status: \(self?.isPremium ?? false)")
            }
        }
    }
    
    private func loadOfferings() {
        Purchases.shared.getOfferings { [weak self] offerings, error in
            DispatchQueue.main.async {
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    print("Error fetching offerings: \(error)")
                    return
                }
                
                self?.offerings = offerings
                print("Loaded offerings: \(offerings?.all.count ?? 0)")
            }
        }
    }
    
    func purchasePremium() async throws {
        guard let offering = offerings?.current else {
            throw PurchaseError.noOffering
        }
        
        guard let premiumPackage = offering.availablePackages.first(where: { $0.storeProduct.productIdentifier.contains("premium") }) else {
            throw PurchaseError.noPremiumPackage
        }
        
        isLoading = true
        
        do {
            let result = try await Purchases.shared.purchase(package: premiumPackage)
            
            DispatchQueue.main.async {
                self.isLoading = false
                
                if result.transaction.transactionState == .purchased {
                    self.checkEntitlements()
                    print("Premium purchase successful")
                }
            }
        } catch {
            DispatchQueue.main.async {
                self.isLoading = false
                self.errorMessage = error.localizedDescription
                print("Purchase failed: \(error)")
            }
            throw error
        }
    }
    
    func restorePurchases() async throws {
        isLoading = true
        
        do {
            try await Purchases.shared.restorePurchases()
            checkEntitlements()
            print("Purchases restored successfully")
        } catch {
            DispatchQueue.main.async {
                self.isLoading = false
                self.errorMessage = error.localizedDescription
                print("Restore failed: \(error)")
            }
            throw error
        }
        
        DispatchQueue.main.async {
            self.isLoading = false
        }
    }
    
    func getPrice(for package: Package) -> String {
        let product = package.storeProduct
        let priceString = product.displayPrice
        let period = product.subscriptionPeriod
        
        if let period = period {
            let unitString: String
            switch period.value {
            case 1:
                switch period.unit {
                case .day:
                    unitString = "day"
                case .week:
                    unitString = "week"
                case .month:
                    unitString = "month"
                case .year:
                    unitString = "year"
                @unknown default:
                    unitString = "period"
                }
            default:
                switch period.unit {
                case .day:
                    unitString = "days"
                case .week:
                    unitString = "weeks"
                case .month:
                    unitString = "months"
                case .year:
                    unitString = "years"
                @unknown default:
                    unitString = "periods"
                }
            }
            return "\(priceString) / \(period.value) \(unitString)"
        }
        
        return priceString
    }
    
    func getTrialPeriod(for package: Package) -> String? {
        let product = package.storeProduct
        let introDiscount = product.introductoryDiscount
        
        guard let introDiscount = introDiscount else { return nil }
        
        let period = introDiscount.subscriptionPeriod
        let priceString = introDiscount.priceLocale.currencySymbol ?? ""
        
        if period.value == 1 {
            switch period.unit {
            case .day:
                return "Free for 1 day"
            case .week:
                return "Free for 1 week"
            case .month:
                return "Free for 1 month"
            case .year:
                return "Free for 1 year"
            @unknown default:
                return nil
            }
        } else {
            switch period.unit {
            case .day:
                return "Free for \(period.value) days"
            case .week:
                return "Free for \(period.value) weeks"
            case .month:
                return "Free for \(period.value) months"
            case .year:
                return "Free for \(period.value) years"
            @unknown default:
                return nil
            }
        }
    }
    
    func showPaywallIfNeeded() -> Bool {
        // Check if user should see paywall (e.g., not premium and hasn't purchased)
        return !isPremium
    }
}

// MARK: - RevenueCat Delegate
extension PurchaseManager: PurchasesDelegate {
    func purchases(_ purchases: Purchases, receivedUpdated customerInfo: CustomerInfo) {
        DispatchQueue.main.async {
            self.customerInfo = customerInfo
            self.isPremium = customerInfo.entitlements["premium"]?.isActive == true
            print("Customer info updated - Premium: \(self.isPremium)")
        }
    }
    
    func purchases(_ purchases: Purchases, didFailToUpdate purchase: Purchase, with transactionFailureReason: StoreKitError) {
        print("Purchase failed: \(transactionFailureReason)")
        DispatchQueue.main.async {
            self.errorMessage = transactionFailureReason.localizedDescription
        }
    }
}

// MARK: - Purchase Errors
enum PurchaseError: LocalizedError {
    case noOffering
    case noPremiumPackage
    case purchaseFailed(String)
    case restoreFailed(String)
    
    var errorDescription: String? {
        switch self {
        case .noOffering:
            return "No offering available"
        case .noPremiumPackage:
            return "No premium package found"
        case .purchaseFailed(let error):
            return "Purchase failed: \(error)"
        case .restoreFailed(let error):
            return "Restore failed: \(error)"
        }
    }
}

// MARK: - Package Model Extension
extension Package {
    var displayName: String {
        switch packageType {
        case .annual:
            return "Annual"
        case .monthly:
            return "Monthly"
        case .weekly:
            return "Weekly"
        case .lifetime:
            return "Lifetime"
        case .twoMonth:
            return "2 Months"
        case .threeMonth:
            return "3 Months"
        case .sixMonth:
            return "6 Months"
        case .custom:
            return "Custom"
        @unknown default:
            return "Unknown"
        }
    }
}

// MARK: - Cancellable Set for Combine
class CancellableSet {
    private var cancellables = Set<AnyCancellable>()
    
    func insert(_ cancellable: AnyCancellable) {
        cancellables.insert(cancellable)
    }
    
    func cancelAll() {
        cancellables.removeAll()
    }
}