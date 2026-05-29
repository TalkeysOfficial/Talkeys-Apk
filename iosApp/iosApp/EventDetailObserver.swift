import Foundation
import sharedKit

/// Observes [EventDetailViewModel.uiState] as a Swift @Published property via SKIE.
@MainActor
final class EventDetailObserver: ObservableObject {
    @Published private(set) var uiState: EventDetailUiState = EventDetailUiStateLoading.shared

    let viewModel: EventDetailViewModel

    init(viewModel: EventDetailViewModel) {
        self.viewModel = viewModel
    }

    func observe() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }

    func retry() {
        viewModel.retry()
    }
}
