import Foundation
import sharedKit

/// Observes CounterViewModel's StateFlow<Int> as a Swift AsyncSequence via SKIE.
/// Uses the documented stable `for await` pattern, not SKIE preview helpers.
@MainActor
final class CounterObserver: ObservableObject {
    @Published private(set) var count: Int = 0
    let viewModel: CounterViewModel

    init(viewModel: CounterViewModel) {
        self.viewModel = viewModel
    }

    func observe() async {
        for await value in viewModel.count {
            self.count = Int(truncating: value)
        }
    }

    func increment() {
        viewModel.increment()
    }
}
