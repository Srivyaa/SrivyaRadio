import Foundation
import GoogleMobileAds
import UIKit

class AdMobManager: NSObject, ObservableObject {
    static let shared = AdMobManager()
    
    @Published var isInterstitialReady = false
    
    private var interstitial: GADInterstitialAd?
    private let purchaseManager = PurchaseManager() // Note: In real app, consider injecting this or observing singleton
    // Better: use PurchaseManager.shared if we made it singleton, or just check UserDefaults for simplicity in this manager
    // to avoid circular dependency or complex DI for now.
    
    private var stationPlayCount = 0
    private let playsBeforeAd = 5
    
    override init() {
        super.init()
        startGoogleMobileAdsSDK()
        loadInterstitial()
    }
    
    private func startGoogleMobileAdsSDK() {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }
    
    // MARK: - Interstitial Ads
    func loadInterstitial() {
        let request = GADRequest()
        // Test Ad Unit ID for Interstitial: ca-app-pub-3940256099942544/4411468910
        GADInterstitialAd.load(withAdUnitID: "ca-app-pub-3940256099942544/4411468910",
                               request: request) { [weak self] ad, error in
            if let error = error {
                print("Failed to load interstitial ad with error: \(error.localizedDescription)")
                return
            }
            self?.interstitial = ad
            self?.interstitial?.fullScreenContentDelegate = self
            self?.isInterstitialReady = true
        }
    }
    
    func showInterstitial(from rootViewController: UIViewController) {
        if let interstitial = interstitial {
            interstitial.present(fromRootViewController: rootViewController)
        } else {
            print("Ad wasn't ready")
            loadInterstitial() // Try loading again
        }
    }
    
    func incrementPlayCountAndCheck(rootViewController: UIViewController?) {
        // Check premium status first
        if PurchaseManager.isPremiumUser { return }
        
        stationPlayCount += 1
        if stationPlayCount >= playsBeforeAd {
            if isInterstitialReady, let rootVC = rootViewController {
                showInterstitial(from: rootVC)
                stationPlayCount = 0
            }
        }
    }
}

extension AdMobManager: GADFullScreenContentDelegate {
    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        loadInterstitial() // Reload for next time
        isInterstitialReady = false
    }
    
    func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Ad failed to present full screen content with error: \(error.localizedDescription)")
        loadInterstitial()
        isInterstitialReady = false
    }
}

// Add static check helper to PurchaseManager for easier access if not already there
extension PurchaseManager {
    static var isPremiumUser: Bool {
        return UserDefaults.standard.bool(forKey: "isPremium")
    }
}
