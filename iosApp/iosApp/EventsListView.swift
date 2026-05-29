import SwiftUI
import sharedKit

/// SwiftUI screen that displays the events list using the shared KMP ViewModel.
///
/// Observes `EventsListViewModel.uiState` via SKIE and renders
/// Loading / Content / Error states. Tapping an event navigates
/// to `EventDetailView`.
struct EventsListView: View {
    @ObservedObject var storeOwner: IosViewModelStoreOwner
    @StateObject private var observer: EventsListObserver

    init(storeOwner: IosViewModelStoreOwner) {
        self.storeOwner = storeOwner
        let vm: EventsListViewModel = storeOwner.viewModel(
            factory: EventsViewModelFactoriesKt.eventsListViewModelFactory
        )
        _observer = StateObject(wrappedValue: EventsListObserver(viewModel: vm))
    }

    var body: some View {
        NavigationStack {
            Group {
                switch onEnum(of: observer.uiState) {
                case .loading:
                    ProgressView("Loading events…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)

                case .content(let content):
                    eventsContent(content)

                case .error(let error):
                    errorView(message: error.message)
                }
            }
            .navigationTitle("Events")
            .task {
                await observer.observe()
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func eventsContent(_ content: EventsListUiStateContent) -> some View {
        VStack(spacing: 0) {
            // Live / Past filter toggle
            Picker("Filter", selection: Binding(
                get: { content.showLiveOnly },
                set: { _ in observer.toggleFilter() }
            )) {
                Text("Live").tag(true)
                Text("Past").tag(false)
            }
            .pickerStyle(.segmented)
            .padding()

            if content.events.isEmpty {
                Spacer()
                Text(content.showLiveOnly ? "No live events" : "No past events")
                    .foregroundColor(.secondary)
                Spacer()
            } else {
                List(content.events, id: \.id) { event in
                    NavigationLink(value: event.id) {
                        EventRowView(event: event)
                    }
                }
                .listStyle(.plain)
                .navigationDestination(for: String.self) { eventId in
                    EventDetailView(storeOwner: storeOwner, eventId: eventId)
                }
            }
        }
    }

    // MARK: - Error

    @ViewBuilder
    private func errorView(message: String) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundColor(.orange)
            Text(message)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            Button("Retry") { observer.retry() }
                .buttonStyle(.borderedProminent)
            Spacer()
        }
    }
}

// MARK: - Event Row

/// A single row in the events list.
private struct EventRowView: View {
    let event: EventSummary

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(event.name)
                .font(.headline)

            HStack {
                Label(event.category, systemImage: "tag")
                Spacer()
                Label(event.mode, systemImage: event.mode == "online" ? "video" : "mappin")
            }
            .font(.subheadline)
            .foregroundColor(.secondary)

            HStack {
                if event.isPaid {
                    Text("₹\(event.ticketPrice)")
                        .font(.subheadline)
                        .foregroundColor(.blue)
                } else {
                    Text("Free")
                        .font(.subheadline)
                        .foregroundColor(.green)
                }
                Spacer()
                Text(event.startDate.prefix(10))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
