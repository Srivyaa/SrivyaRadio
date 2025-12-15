import SwiftUI
import StoreKit

struct AboutScreen: View {
    @Environment(\.openURL) var openURL
    @State private var showingReviewPrompt = false
    
    private let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    private let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // App Icon and Info
                VStack(spacing: 16) {
                    Image("AppIcon")
                        .resizable()
                        .frame(width: 100, height: 100)
                        .cornerRadius(20)
                        .shadow(radius: 10)
                    
                    VStack(spacing: 4) {
                        Text("SrivyaRadio")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                        
                        Text("Version \(appVersion) (\(buildNumber))")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.top, 20)
                
                // Description
                VStack(alignment: .leading, spacing: 12) {
                    Text("About SrivyaRadio")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    Text("""
                    SrivyaRadio is your gateway to thousands of radio stations from around the world. Discover new music, stay connected to your favorite stations, and enjoy high-quality streaming wherever you are.

                    Features:
                    • Stream live radio from over 100 countries
                    • Create and manage your favorites
                    • Download stations for offline listening (Premium)
                    • Sleep timer for peaceful bedtime listening
                    • Search and discover new stations
                    • Clean, intuitive interface
                    """)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.leading)
                }
                .padding(.horizontal)
                
                // Quick Actions
                VStack(spacing: 12) {
                    Button(action: requestReview) {
                        HStack {
                            Image(systemName: "star.fill")
                            Text("Rate SrivyaRadio")
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(8)
                    }
                    
                    Button(action: shareApp) {
                        HStack {
                            Image(systemName: "square.and.arrow.up")
                            Text("Share SrivyaRadio")
                        }
                        .font(.headline)
                        .foregroundColor(.blue)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue.opacity(0.1))
                        .cornerRadius(8)
                    }
                }
                .padding(.horizontal)
                
                // Links Section
                VStack(spacing: 16) {
                    Text("Connect With Us")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    VStack(spacing: 12) {
                        Link(destination: URL(string: "https://github.com/srivyaa/srivyaradio")!) {
                            HStack {
                                Image(systemName: "globe")
                                Text("Website")
                                Spacer()
                                Image(systemName: "link")
                            }
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        }
                        
                        Link(destination: URL(string: "https://twitter.com/srivyaradio")!) {
                            HStack {
                                Image(systemName: "bird")
                                Text("Twitter")
                                Spacer()
                                Image(systemName: "link")
                            }
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        }
                        
                        Link(destination: URL(string: "https://instagram.com/srivyaradio")!) {
                            HStack {
                                Image(systemName: "camera")
                                Text("Instagram")
                                Spacer()
                                Image(systemName: "link")
                            }
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        }
                        
                        Link(destination: URL(string: "mailto:support@srivyaradio.com")!) {
                            HStack {
                                Image(systemName: "envelope")
                                Text("Email Support")
                                Spacer()
                                Image(systemName: "link")
                            }
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        }
                    }
                }
                .padding(.horizontal)
                
                // Technical Info
                VStack(spacing: 12) {
                    Text("Technical Information")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("Platform:")
                            Spacer()
                            Text("iOS")
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Text("Minimum iOS Version:")
                            Spacer()
                            Text("15.0")
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Text("Database:")
                            Spacer()
                            Text("Core Data")
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Text("Audio Framework:")
                            Spacer()
                            Text("AVFoundation")
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Text("UI Framework:")
                            Spacer()
                            Text("SwiftUI")
                                .foregroundColor(.secondary)
                        }
                    }
                    .font(.body)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                }
                .padding(.horizontal)
                
                // Legal
                VStack(spacing: 12) {
                    Text("Legal")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    VStack(spacing: 8) {
                        Button("Privacy Policy") {
                            openURL(URL(string: "https://srivyaradio.com/privacy")!)
                        }
                        .foregroundColor(.blue)
                        
                        Button("Terms of Service") {
                            openURL(URL(string: "https://srivyaradio.com/terms")!)
                        }
                        .foregroundColor(.blue)
                        
                        Text("""
                        All radio station content is provided by third-party sources.
                        SrivyaRadio does not host or control the streamed content.
                        """)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)
                    }
                }
                .padding(.horizontal)
                
                Spacer(minLength: 20)
            }
        }
        .navigationTitle("About")
        .sheet(isPresented: $showingReviewPrompt) {
            ReviewPromptView()
        }
    }
    
    private func requestReview() {
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
            SKStoreReviewController.requestReview(in: scene)
        }
    }
    
    private func shareApp() {
        let activityItems = [
            "Check out SrivyaRadio - The best radio streaming app! Download it from the App Store."
        ]
        
        let activityController = UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let window = windowScene.windows.first {
            window.rootViewController?.present(activityController, animated: true)
        }
    }
}

struct ReviewPromptView: View {
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Image(systemName: "star.circle.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.yellow)
                
                Text("Enjoying SrivyaRadio?")
                    .font(.title)
                    .fontWeight(.bold)
                
                Text("""
                Your feedback helps us improve the app and provide a better experience for everyone.
                """)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                
                Button("Leave a Review") {
                    if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
                        SKStoreReviewController.requestReview(in: scene)
                    }
                    dismiss()
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .cornerRadius(8)
                
                Button("Maybe Later") {
                    dismiss()
                }
                .foregroundColor(.blue)
            }
            .padding()
            .navigationTitle("Rate SrivyaRadio")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}