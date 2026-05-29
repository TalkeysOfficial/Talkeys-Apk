import Foundation
import sharedKit

/// Observes [EventsListViewModel.uiState] as a Swift @Published property via SKIE.
///
/// Usage in SwiftUI:
///   @StateObject private var observer = EventsListObserver(viewModel: vm)
///   // observer.uiState drives the view
@MainActor
final class EventsListObserver: ObservableObject {
    @Published private(set) var uiState: EventsListUiState = EventsListUiStateLoading.shared

    let viewModel: EventsListViewModel

    init(viewModel: EventsListViewModel) {
        self.viewModel = viewModel
    }

    func observe() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }

    func toggleFilter() {
        viewModel.toggleFilter()
    }

    func retry() {
        viewModel.retry()
    }
}
