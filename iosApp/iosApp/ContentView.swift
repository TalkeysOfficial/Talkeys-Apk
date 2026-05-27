import SwiftUI
import sharedKit

struct ContentView: View {
    @StateObject private var storeOwner = IosViewModelStoreOwner()

    var body: some View {
        CounterDemoView(storeOwner: storeOwner)
    }
}

/// Separate view so we can create the observer after storeOwner is initialized.
private struct CounterDemoView: View {
    @ObservedObject var storeOwner: IosViewModelStoreOwner
    @StateObject private var counter: CounterObserver

    init(storeOwner: IosViewModelStoreOwner) {
        self.storeOwner = storeOwner
        let vm: CounterViewModel = storeOwner.viewModel(
            factory: CounterViewModelFactoryKt.counterViewModelFactory
        )
        _counter = StateObject(wrappedValue: CounterObserver(viewModel: vm))
    }

    var body: some View {
        VStack(spacing: 24) {
            Text(SharedApp.shared.greeting())
                .font(.title2)
                .multilineTextAlignment(.center)
                .padding()

            Divider()

            Text("Counter: \(counter.count)")
                .font(.title)
                .monospacedDigit()

            Button(action: { counter.increment() }) {
                Text("Increment")
                    .font(.headline)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
            }
            .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            await counter.observe()
        }
    }
}
