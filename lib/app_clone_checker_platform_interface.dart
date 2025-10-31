import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'app_clone_checker_method_channel.dart';

abstract class AppCloneCheckerPlatform extends PlatformInterface {
  /// Constructs a AppCloneCheckerPlatform.
  AppCloneCheckerPlatform() : super(token: _token);

  static final Object _token = Object();

  static AppCloneCheckerPlatform _instance = MethodChannelAppCloneChecker();

  /// The default instance of [AppCloneCheckerPlatform] to use.
  ///
  /// Defaults to [MethodChannelAppCloneChecker].
  static AppCloneCheckerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AppCloneCheckerPlatform] when
  /// they register themselves.
  static set instance(AppCloneCheckerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
