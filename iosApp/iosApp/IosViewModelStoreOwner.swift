import Foundation
import sharedKit

/// Reusable iOS lifecycle bridge for AndroidX KMP ViewModels.
///
/// Conforms to `ViewModelStoreOwner` so it participates in the
/// AndroidX lifecycle contract. Each SwiftUI view that needs shared
/// ViewModels creates an instance of this class (typically via
/// @StateObject). On deinit the ViewModelStore is cleared, matching
/// Android's lifecycle semantics.
///
/// See: https://developer.android.com/kotlin/multiplatform/viewmodel
final class IosViewModelStoreOwner: ViewModelStoreOwner, ObservableObject {
    private let store = ViewModelStore()

    // MARK: - ViewModelStoreOwner

    var viewModelStore: ViewModelStore { store }

    // MARK: - Generic ViewModel resolution

    /// Resolve a shared ViewModel using the generic AndroidX KMP bridge.
    ///
    /// - Parameters:
    ///   - key: Optional key to distinguish multiple instances of the same type.
    ///   - factory: A `ViewModelProviderFactory` that knows how to create the ViewModel.
    ///   - extras: Optional `CreationExtras`; defaults to `CreationExtras.Empty.shared`.
    /// - Returns: The resolved ViewModel, cast to `T`.
    func viewModel<T: ViewModel>(
        key: String? = nil,
        factory: ViewModelProviderFactory,
        extras: CreationExtras? = nil
    ) -> T {
        // swiftlint:disable:next force_try force_cast
        return try! store.resolveViewModel(
            objCClass: T.self,
            key: key,
            factory: factory,
            extras: extras ?? CreationExtras.Empty.shared
        ) as! T
    }

    deinit {
        store.clear()
    }
}
