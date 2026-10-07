# FreeShop Billing

A professional Android billing and invoicing application built with Jetpack Compose, Room Database, and Material 3. Designed for small businesses to create invoices, quotations, manage customers, products, and generate professional PDFs.

## Features

- **Authentication**: Register and login with secure password hashing (SHA-256 + salt)
- **Dashboard**: Create invoices and quotations with real-time calculations
- **Bills History**: View, filter, search, share, and delete bills/quotations
- **Convert Quotations**: Convert quotations to invoices with one tap
- **Customers Management**: Add, view, and delete customers with GSTIN support
- **Items/Products Management**: Manage product catalog with HSN codes, tax rates, and units
- **Settings**: Configure shop details, address, tax settings, bank details, logo, and terms
- **PDF Generation**: Professional PDF invoices/quotations with shop branding
- **Offline-first**: All data stored locally using Room database
- **Multi-shop support**: Each shop has isolated data

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with StateFlow
- **Database**: Room (SQLite)
- **Dependency Injection**: Manual (ViewModel factories)
- **Navigation**: Navigation Compose
- **PDF Generation**: Android PdfDocument API
- **Image Loading**: Coil
- **DataStore**: Preferences DataStore for session management
- **Coroutines & Flow**: Kotlin Coroutines for async operations
- **Build System**: Gradle with Kotlin DSL (KTS)
- **Version Catalog**: TOML-based dependency management

## Requirements

- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android SDK 34 (compileSdk)
- Minimum SDK: 26 (Android 8.0 Oreo)
- Target SDK: 34

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd freeshop-billing-android
   ```

2. Open in Android Studio:
   - File → Open → Select the `freeshop-billing-android` folder
   - Wait for Gradle sync to complete

3. Build and run:
   - Connect an Android device or start an emulator (API 26+)
   - Click Run (▶️) or press `Shift+F10`

## Configuration

No external configuration required. The app uses:
- **Local database**: Room SQLite database (`freeshop.db`) stored in app's private storage
- **Session**: Preferences DataStore for login persistence
- **File storage**: PDFs and logos stored in app's internal files directory

## Project Structure

```
freeshop-billing-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/freeshop/billing/
│   │   │   ├── MainActivity.kt           # Entry point
│   │   │   ├── data/                     # Data layer
│   │   │   │   ├── PasswordHasher.kt     # Secure password hashing
│   │   │   │   ├── SessionStore.kt       # Session management
│   │   │   │   ├── db/                   # Room database
│   │   │   │   │   ├── AppDatabase.kt    # Database configuration
│   │   │   │   │   ├── dao/              # Data Access Objects
│   │   │   │   │   └── entity/           # Database entities
│   │   │   │   └── repository/           # Repository pattern
│   │   │   ├── domain/                   # Business logic
│   │   │   │   ├── Money.kt              # Currency formatting & words
│   │   │   │   ├── PaymentStatus.kt      # Payment status enum
│   │   │   │   └── Validators.kt         # Input validation
│   │   │   ├── pdf/                      # PDF generation
│   │   │   │   └── BillPdfGenerator.kt   # Professional PDF creation
│   │   │   └── ui/                       # UI layer
│   │   │       ├── components/           # Reusable UI components
│   │   │       ├── nav/                  # Navigation graph
│   │   │       ├── screen/               # Screen composables
│   │   │       ├── theme/                # Material 3 theming
│   │   │       └── viewmodel/            # ViewModels (MVVM)
│   │   ├── res/
│   │   │   ├── values/                   # Strings, themes
│   │   │   └── xml/                      # FileProvider paths
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                # Version catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
└── .gitignore
```

## Screenshots

| Screen | Description |
|--------|-------------|
| Landing | Welcome screen with register/login options |
| Register | Create new shop account |
| Login | Sign in to existing shop |
| Dashboard | Create invoices/quotations with items |
| Bills | History with filters, search, share, delete |
| Customers | Manage customer list |
| Items | Product catalog management |
| Settings | Shop profile, tax, bank details, logo |

*Add screenshots to `docs/screenshots/` folder*

## Future Improvements

- [ ] Export data (CSV/Excel)
- [ ] Backup & restore to cloud (Google Drive)
- [ ] Multi-language support
- [ ] Dark mode theme
- [ ] Barcode/QR code scanning for products
- [ ] Payment gateway integration
- [ ] GST return filing (GSTR-1)
- [ ] Multi-user support per shop
- [ ] Inventory tracking with stock alerts
- [ ] Recurring invoices
- [ ] Email/SMS integration for sending bills
- [ ] Custom invoice templates

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/) design system
- [Room](https://developer.android.com/training/data-storage/room) for local persistence
- [Coil](https://coil-kt.github.io/coil/) for image loading