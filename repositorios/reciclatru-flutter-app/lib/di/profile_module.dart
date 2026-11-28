/// Registra las dependencias de profile module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/local/profile/android_geocoder_data_source.dart';
import '../data/repository/profile/profile_repository_impl.dart' as implementation;
import '../data/repository/profile/user_profile_repository_impl.dart' as user_profile_implementation;
import '../data/remote/api/profile/profile_api.dart';
import '../data/repository/profile/geocoding_repository_impl.dart';
import '../domain/repository/profile/geocoding_repository.dart';
import '../di/network_module.dart';
import '../domain/repository/profile/profile_repository.dart';
import '../domain/repository/profile/user_profile_repository.dart';
import '../domain/usecase/profile/get_profile_use_case.dart';
import '../domain/usecase/profile/describe_point_use_case.dart';
import '../domain/usecase/profile/search_address_use_case.dart';
import '../domain/usecase/profile/suggest_addresses_use_case.dart';
import '../domain/usecase/profile/update_profile_use_case.dart';
import '../domain/usecase/profile/save_pickup_location_use_case.dart';

/// Provee las operaciones del punto de recojo y los datos personales por separado.
final profileApiProvider = Provider<ProfileApi>((ref) => ProfileApi(ref.watch(dioProvider)));
final profileRepositoryProvider = Provider<ProfileRepository>((ref) =>
    implementation.ProfileRepositoryImpl(ref.watch(profileApiProvider)));
final userProfileRepositoryProvider = Provider<UserProfileRepository>((ref) =>
    user_profile_implementation.UserProfileRepositoryImpl(ref.watch(profileApiProvider)));

/// Caso de uso del perfil requerido al publicar un pedido.
final getProfileUseCaseProvider = Provider<GetProfileUseCase>((ref) =>
    GetProfileUseCase(ref.watch(userProfileRepositoryProvider)));
final updateProfileUseCaseProvider = Provider<UpdateProfileUseCase>((ref) =>
    UpdateProfileUseCase(ref.watch(userProfileRepositoryProvider)));
final savePickupLocationUseCaseProvider = Provider<SavePickupLocationUseCase>((ref) =>
    SavePickupLocationUseCase(ref.watch(profileRepositoryProvider)));

/// Provee búsqueda de direcciones y lectura del punto seleccionado en el mapa.
final geocodingRepositoryProvider = Provider<GeocodingRepository>((ref) {
  // Android usa el Geocoder del sistema; la búsqueda no necesita token ni permiso GPS.
  final repository = GeocodingRepositoryImpl(AndroidGeocoderDataSource());
  repository.initialize();
  return repository;
});

/// Casos de uso usados por la pantalla de ubicación.
final describePointUseCaseProvider = Provider<DescribePointUseCase>((ref) => DescribePointUseCase(ref.watch(geocodingRepositoryProvider)));
final searchAddressUseCaseProvider = Provider<SearchAddressUseCase>((ref) => SearchAddressUseCase(ref.watch(geocodingRepositoryProvider)));
final suggestAddressesUseCaseProvider = Provider<SuggestAddressesUseCase>((ref) => SuggestAddressesUseCase(ref.watch(geocodingRepositoryProvider)));
