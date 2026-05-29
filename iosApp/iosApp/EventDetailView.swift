import SwiftUI
import sharedKit

/// SwiftUI screen that displays full event detail using the shared KMP ViewModel.
struct EventDetailView: View {
    @ObservedObject var storeOwner: IosViewModelStoreOwner
    @StateObject private var observer: EventDetailObserver

    private let eventId: String

    init(storeOwner: IosViewModelStoreOwner, eventId: String) {
        self.storeOwner = storeOwner
        self.eventId = eventId
        let vm: EventDetailViewModel = storeOwner.viewModel(
            key: eventId,
            factory: EventsViewModelFactoriesKt.eventDetailViewModelFactory
        )
        _observer = StateObject(wrappedValue: EventDetailObserver(viewModel: vm))
    }

    var body: some View {
        Group {
            switch onEnum(of: observer.uiState) {
            case .loading:
                ProgressView("Loading details…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .content(let content):
                detailContent(content.event)

            case .error(let error):
                errorView(message: error.message)
            }
        }
        .navigationTitle("Event Detail")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            observer.viewModel.loadEvent(eventId: eventId, forceRefresh: false)
            await observer.observe()
        }
    }

    // MARK: - Detail Content

    @ViewBuilder
    private func detailContent(_ event: EventDetail) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header
                Text(event.name)
                    .font(.title)
                    .fontWeight(.bold)

                // Category & mode
                HStack {
                    Label(event.category, systemImage: "tag")
                    Spacer()
                    Label(event.mode, systemImage: event.mode == "online" ? "video" : "mappin")
                }
                .foregroundColor(.secondary)

                Divider()

                // Date & time
                VStack(alignment: .leading, spacing: 8) {
                    Label(String(event.startDate.prefix(10)), systemImage: "calendar")
                    Label(event.startTime, systemImage: "clock")
                    Label(event.duration, systemImage: "hourglass")
                }

                Divider()

                // Pricing & seats
                HStack {
                    if event.isPaid {
                        Text("₹\(event.ticketPrice)")
                            .font(.title2)
                            .foregroundColor(.blue)
                    } else {
                        Text("Free")
                            .font(.title2)
                            .foregroundColor(.green)
                    }
                    Spacer()
                    VStack(alignment: .trailing) {
                        Text("\(event.availableSeats) / \(event.totalSeats) seats")
                            .font(.subheadline)
                        Text("\(event.registrationCount) registered")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                // Description
                if let desc = event.eventDescription, !desc.isEmpty {
                    Divider()
                    Text("About")
                        .font(.headline)
                    Text(desc)
                        .font(.body)
                }

                // Organizer
                if let name = event.organizerName, !name.isEmpty {
                    Divider()
                    Text("Organizer")
                        .font(.headline)
                    Text(name)
                        .font(.body)
                }

                // Prizes
                if let prizes = event.prizes, !prizes.isEmpty {
                    Divider()
                    Text("Prizes")
                        .font(.headline)
                    Text(prizes)
                        .font(.body)
                }
            }
            .padding()
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
