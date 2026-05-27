import SwiftUI
import sharedKit

struct ContentView: View {
    var body: some View {
        VStack(spacing: 24) {
            Text(SharedApp.shared.greeting())
                .font(.title2)
                .multilineTextAlignment(.center)
                .padding()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
