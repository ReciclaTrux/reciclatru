/// Implementa el acceso local de shared prefs location prompt store para persistencia o servicios del dispositivo.
import 'package:shared_preferences/shared_preferences.dart';

import '../../../domain/repository/explore/location_prompt_store.dart';

/// Guarda únicamente si el aviso de ubicación ya se mostró.
class SharedPrefsLocationPromptStore implements LocationPromptStore {
  static const _fileKey = 'explore';
  static const _shownKey = 'location_prompt_shown';

  @override
  Future<bool> wasShown() async {
    final preferences = await SharedPreferences.getInstance();
    return preferences.getBool('$_fileKey/$_shownKey') ?? false;
  }

  @override
  Future<void> markShown() async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setBool('$_fileKey/$_shownKey', true);
  }
}
