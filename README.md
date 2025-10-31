# App Clone Checker

A Flutter plugin to detect Android app cloning and unauthorized execution environments.

## Features

- Detects app cloning through multiple methods:
  - Package path analysis
  - Dual app environment detection (ID 999)
  - Work Profile detection (with Samsung-specific handling)
- Returns specific error codes for easy handling
- Optional Work Profile allowance

## Platform Support

**Android only** - iOS is not supported

## Usage

### Basic Usage

```dart
final result = await AppCloneChecker.appOriginality("com.your.package.name");

print("Result: ${result['result']}");
print("Code: ${result['code']}");
print("Message: ${result['message']}");
print("System Manager: ${result['systemManager']}");
```

### With Work Profile Configuration

```dart
final result = await AppCloneChecker.appOriginality(
  "com.your.package.name",
  isWorkProfileAllowed: false  // Set to true to allow Work Profile execution
);
```

### Handling Different Cases

```dart
final result = await AppCloneChecker.appOriginality("com.your.package.name");

switch (result['code']) {
  case 'Success':
    // App is valid and running in legitimate environment
    print('✓ Valid app');
    print('  System Manager: ${result['systemManager']}');
    break;

  case 'ERROR_PACKAGE_MISMATCH':
    // Clone detected: package path has been modified
    print('✗ Clone detected: Package path mismatch');
    break;

  case 'ERROR_DUAL_APP_999':
    // Clone detected: running in dual app environment
    print('✗ Clone detected: Dual app environment');
    break;

  case 'ERROR_WORK_PROFILE_SAMSUNG':
    // Running in Samsung Work Profile or Secure Folder
    print('✗ Samsung Work Profile/Secure Folder detected');
    print('  Detected managers: ${result['systemManager']}');
    break;

  case 'ERROR_WORK_PROFILE_DETECTED':
    // Running in generic Work Profile with unauthorized MDM
    print('✗ Unauthorized Work Profile detected');
    print('  Non-whitelisted managers: ${result['systemManager']}');
    break;

  case 'ERROR_NO_APP_ID':
    // Application ID parameter was not provided
    print('✗ Error: No application ID provided');
    break;

  case 'ERROR_NO_ACTIVITY_CONTEXT':
    // Internal error: Activity context not available
    print('✗ Internal error');
    break;

  default:
    print('✗ Unknown error');
}
```

### Advanced: Analyzing System Managers

```dart
final result = await AppCloneChecker.appOriginality("com.your.package.name");

if (result['code'] == 'Success') {
  final managers = result['systemManager'] as String;

  if (managers.isEmpty) {
    print('No device policy managers detected');
  } else if (managers.contains('com.zte.mdm')) {
    print('Running on ZTE MyOS device');
  } else if (managers.contains('com.samsung.android.knox')) {
    print('Running on Samsung Knox device');
  } else if (managers.contains('com.google.android.gms') && !managers.contains(',')) {
    print('Standard Android device with GMS only');
  }
}
```

## Response Format

The plugin returns a `Map<String, dynamic>` with four keys:

| Key | Type | Description |
|-----|------|-------------|
| `result` | String | `"Success"` or `"Failure"` |
| `code` | String | Specific code for programmatic handling |
| `message` | String | Human-readable description |
| `systemManager` | String | Detected device policy managers (comma-separated) |

### System Manager Examples

The `systemManager` field will contain the package names of detected device policy managers:

- **Google devices**: `com.google.android.gms`
- **Samsung devices**: `com.google.android.gms, com.samsung.android.knox`
- **ZTE MyOS**: `com.zte.mdm` or `com.zte.systemmanager`
- **Xiaomi MIUI**: `com.xiaomi.miui.securitycenter`
- **Oppo ColorOS**: `com.oppo.safe, com.coloros.safecenter`
- **Enterprise MDM**: `com.airwatch.mdm`, `com.microsoft.intune`, etc.
- **No managers**: Empty string `""`

### Response Codes

#### Success
- **`Success`** - App is valid and not cloned

#### Failure Codes
- **`ERROR_NO_APP_ID`** - Application ID parameter not provided
- **`ERROR_PACKAGE_MISMATCH`** - Package path contains unexpected structure (possible clone)
- **`ERROR_DUAL_APP_999`** - Dual app environment detected (user ID 999)
- **`ERROR_WORK_PROFILE_SAMSUNG`** - Samsung Work Profile or Secure Folder detected
- **`ERROR_WORK_PROFILE_DETECTED`** - Generic Work Profile with non-GMS policy detected
- **`ERROR_NO_ACTIVITY_CONTEXT`** - Activity context not available (internal error)

## How It Works

The plugin uses multiple detection methods:

1. **Package Path Analysis**: Checks if the app's file system path has been modified
2. **Dual App ID Detection**: Looks for user ID `999` commonly used by clone apps
3. **Work Profile Detection**: Uses Android's DevicePolicyManager to detect Work Profiles
   - Special handling for Samsung devices (Secure Folder)
   - **Whitelist of legitimate OEM system managers** including:
     - Google (GMS)
     - Samsung (Knox)
     - ZTE (MyOS MDM)
     - Xiaomi (MIUI Security)
     - Oppo, Vivo, Huawei, OnePlus, Realme, Motorola
   - Flags non-whitelisted MDM as unauthorized enterprise profiles

### ZTE MyOS Support

The plugin fully supports ZTE MyOS devices:
- ✅ **China ROM** (without Google Services): Recognizes `com.zte.mdm` and `com.zte.systemmanager` as legitimate
- ✅ **Global ROM** (with Google Services): Works with both GMS and ZTE managers
- ✅ **App Twin**: Detects ZTE's native dual app feature through path analysis

## Supported Devices & OEMs

This plugin has been tested and whitelists the following manufacturers:

| Manufacturer | System Manager | Status |
|--------------|----------------|--------|
| **Google** | `com.google.android.gms` | ✅ Supported |
| **Samsung** | `com.samsung.android.knox` | ✅ Supported (Knox/Secure Folder) |
| **ZTE** | `com.zte.mdm`, `com.zte.systemmanager` | ✅ Supported (MyOS China/Global) |
| **Xiaomi** | `com.xiaomi.miui.securitycenter` | ✅ Supported (MIUI Second Space) |
| **Oppo** | `com.oppo.safe`, `com.coloros.safecenter` | ✅ Supported (ColorOS) |
| **Vivo** | `com.vivo.securitycore` | ✅ Supported |
| **Huawei** | `com.huawei.systemmanager` | ✅ Supported |
| **OnePlus** | `com.oneplus.security` | ✅ Supported (OxygenOS) |
| **Realme** | `com.realme.securitycheck` | ✅ Supported (Realme UI) |
| **Motorola** | `com.motorola.devicemanagement` | ✅ Supported |

### What Gets Detected

✅ **Legitimate (Won't Block)**:
- Official OEM dual app features (Samsung Secure Folder, Xiaomi Second Space, etc.)
- Standard Google Mobile Services
- Manufacturer security centers

❌ **Will Be Detected**:
- Third-party cloning apps (Parallel Space, Island, Shelter, etc.)
- Enterprise MDM not in whitelist (AirWatch, MobileIron, custom MDM)
- Modified file system paths
- Dual app ID 999 environments

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  app_clone_checker: ^2.0.2
```

Then run:
```bash
flutter pub get
```

## Use Cases

### ✅ When to Use This Plugin

- **Banking & Financial Apps**: Prevent unauthorized cloning for security compliance
- **Gaming Apps**: Prevent multi-accounting and cheating
- **License Enforcement**: Ensure one installation per device
- **Corporate Apps**: Detect unauthorized work profiles
- **DRM Content**: Prevent content duplication

### ⚠️ When NOT to Use

- **Enterprise Apps**: If your app is meant for corporate MDM environments
- **Consumer Apps**: That benefit from work/personal separation

## Limitations

- **Android Only**: No iOS support (iOS has different app sandboxing)
- **Not 100% Foolproof**: Sophisticated cloners may evade detection
- **OEM Variations**: New Android manufacturers may need whitelist updates
- **False Positives Possible**: If new OEM system managers are released

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## Contributing

Found a new OEM system manager that should be whitelisted? Please open an issue or PR on [GitHub](https://github.com/vickyponraj/app-clone-checker).

## Credits

This project is hosted on [pub.dev](https://pub.dev/packages/app_clone_checker/)

Inspired by:
- [ProAndroidDev - Preventing Android App Cloning](https://proandroiddev.com/preventing-android-app-cloning-e3194269bcfa)
- [StackOverflow - Preventing App Cloning](https://stackoverflow.com/questions/48900083/preventing-an-android-app-being-cloned-by-an-app-cloner/67353578#67353578)

## License

See [LICENSE](LICENSE) file for details.

