# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0.0-Alpha] - 2025-12-11

### Removed
- **Splash Screen**: Removed the splash screen to improve app launch time and provide a more direct user experience
- **Unused Resources**: Cleaned up unused splash screen resources and activities

### Changed
- **App Launch Flow**: Updated the app to launch directly into the main activity
- **Version Bump**: Updated versionCode to 3 and versionName to 1.2.0.0-Devotional

### Fixed
- **Build Warnings**: Resolved any build warnings related to the removed splash screen components

## [1.1.0.0-Devotional] - 2025-12-03

### Added
- **Devotional JSON Format Support**: Complete implementation of new devotional JSON format support alongside existing radio station format
- **Unified Station Model**: New `UnifiedStation` entity supporting both regular radio stations and devotional items
- **Database Migration**: Added `unified_stations` table with automatic migration from v2 to v3
- **Extended Media Support**: Enhanced `MediaItemFactory` to handle devotional station metadata
- **Background Processing**: New `DownloadDevotionalWorker` for async devotional data fetching
- **API Extensions**: Updated `StationsInterface` and `StationsClient` for multiple JSON format support
- **Repository Layer**: Added `StationsRepository` with conversion methods between formats
- **Utility Classes**: Created `StationConverter` for format conversion and `DevotionalUsageExample` for documentation

### Changed
- **Database Version**: Updated from v2 to v3 with unified_stations table migration
- **API Version**: Incremented to 1.1.0.0-Devotional
- **Constants**: Added `DEVOTIONAL_ID`, `DEVOTIONAL_BASE_URL`, and `DEVOTIONAL_DATA_PATH` constants

### Technical Details
- **Backward Compatibility**: Maintained full compatibility with existing radio station JSON format
- **Data Models**: 
  - Created `DevotionalData.kt` for nested devotional structure
  - Created `DevotionalItem.kt` for individual devotional tracks
  - Created `UnifiedStation.kt` for unified data model
- **Database Schema**: New `unified_stations` table with 25+ fields supporting both station types
- **Media Integration**: Enhanced media player support with extended metadata handling
- **Documentation**: Added comprehensive `DEVOTIONAL_JSON_SUPPORT.md` with usage examples

### Supported Formats
1. **Existing Format**: Array of radio stations with basic metadata
2. **New Devotional Format**: Structured format with folders containing devotional items

## [1.0.0.0-Test] - 2025-11-06

### Added
- Initial test release of SrivyaRadio Android application
- Basic radio station support
- Media player integration with ExoPlayer
- Favorites and offline functionality
- Country-based station browsing
- Search functionality