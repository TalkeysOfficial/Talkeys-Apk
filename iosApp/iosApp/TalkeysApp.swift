import SwiftUI
import sharedKit

@main
struct TalkeysApp: App {
    init() {
        SharedApp.shared.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
