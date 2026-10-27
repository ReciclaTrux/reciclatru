package pe.edu.upn.reciclatru.core.di

import javax.inject.Qualifier

/** Dispatcher para trabajo de entrada/salida (red, disco, Geocoder). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Alcance que vive lo mismo que la app, para flujos compartidos como la sesión. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
