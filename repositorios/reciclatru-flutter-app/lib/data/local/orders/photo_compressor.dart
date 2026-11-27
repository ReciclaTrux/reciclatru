/// Implementa el acceso local de photo compressor para persistencia o servicios del dispositivo.
import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:image_picker/image_picker.dart';
import 'package:image/image.dart' as img;

/// Selecciona fotos y reproduce el procesamiento de `PhotoCompressor.kt` al subirlas.
class PhotoCompressor {
  PhotoCompressor([ImagePicker? picker]) : _picker = picker ?? ImagePicker();
  final ImagePicker _picker;

  /// Conserva el original seleccionado; Kotlin comprime el contenido al momento de subirlo.
  Future<XFile?> pick({required ImageSource source}) => _picker.pickImage(
    source: source,
  );

  /// Reduce una foto a JPEG y realiza el trabajo de imagen fuera del isolate de UI.
  Future<Uint8List> compress(String path) async {
    final original = await File(path).readAsBytes();
    return Isolate.run(() => _compressBytes(original));
  }
}

/// Decodifica, corrige la orientación EXIF, reduce dimensiones y ajusta la calidad.
Uint8List _compressBytes(Uint8List original) {
  final decoded = img.decodeImage(original);
  if (decoded == null) throw const FormatException('No se pudo leer la foto');

  // Corrige la orientación que las cámaras guardan en los metadatos EXIF.
  final oriented = img.bakeOrientation(decoded);
  final longestSide = oriented.width > oriented.height
      ? oriented.width
      : oriented.height;
  final resized = longestSide > _maxSidePx
      ? img.copyResize(
          oriented,
          width: oriented.width >= oriented.height ? _maxSidePx : null,
          height: oriented.height > oriented.width ? _maxSidePx : null,
          // El filtrado lineal equivale al escalado filtrado de Bitmap en Kotlin.
          interpolation: img.Interpolation.linear,
        )
      : oriented;

  // Reduce la calidad en pasos de diez hasta 35 %, como la implementación Kotlin.
  var quality = _initialQuality;
  var bytes = Uint8List.fromList(img.encodeJpg(resized, quality: quality));
  while (bytes.lengthInBytes > _targetBytes && quality > _minQuality) {
    quality -= _qualityStep;
    bytes = Uint8List.fromList(img.encodeJpg(resized, quality: quality));
  }
  return bytes;
}

const int _maxSidePx = 1280;
const int _targetBytes = 300 * 1024;
const int _initialQuality = 85;
const int _minQuality = 35;
const int _qualityStep = 10;
