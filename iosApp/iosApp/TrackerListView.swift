import SwiftUI
import shared

/// Renders whatever the gateway returns — no status/date-math happens here,
/// that logic lives in `shared`'s commonMain (StatusCalculator, RelativeTimeFormatter).
struct TrackerListView: View {
    private let gateway = IosTrackerGateway()

    @State private var trackers: [Tracker] = []

    var body: some View {
        NavigationStack {
            Group {
                if trackers.isEmpty {
                    Text(IosStrings.emptyList)
                        .foregroundStyle(.secondary)
                } else {
                    List(trackers, id: \.id) { tracker in
                        TrackerRow(tracker: tracker)
                    }
                }
            }
            .navigationTitle(IosStrings.navigationTitle)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        Task { await addSampleTracker() }
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel(IosStrings.addTrackerAccessibilityLabel)
                }
            }
            .task {
                await loadTrackers()
            }
        }
    }

    private func loadTrackers() async {
        do {
            trackers = try await gateway.getTrackers()
        } catch {
            trackers = []
        }
    }

    private func addSampleTracker() async {
        do {
            _ = try await gateway.addSampleTracker()
            await loadTrackers()
        } catch {
            // swallow: this screen is a placeholder, not the real check-in flow yet
        }
    }
}

private struct TrackerRow: View {
    let tracker: Tracker

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("\(tracker.emoji) \(tracker.name)")
                .font(.headline)
            Text("\(tracker.category) · \(targetLabel)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
    }

    private var targetLabel: String {
        guard let days = tracker.targetDays else { return IosStrings.noTarget }
        return IosStrings.targetDays(days.intValue)
    }
}

#Preview {
    TrackerListView()
}
