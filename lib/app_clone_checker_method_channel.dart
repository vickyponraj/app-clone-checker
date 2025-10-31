import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'app_clone_checker_platform_interface.dart';

/// An implementation of [AppCloneCheckerPlatform] that uses method channels.
class MethodChannelAppCloneChecker extends AppCloneCheckerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('app_clone_checker');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
