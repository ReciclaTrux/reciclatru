package pe.edu.upn.reciclatru.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.reciclatru.core.di.ApplicationScope
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Única fuente de la sesión en la app. Expone un [StateFlow] para las guardas de
 * navegación y el token para el interceptor HTTP.
 *
 * La sesión la crea y la borra `feature/auth` (HU-01/HU-02); acá solo se guarda. En el
 * Release 1 el token va en DataStore del dispositivo; si más adelante se exige cifrado
 * en reposo, se cambia esta implementación sin tocar a quien la consume.
 */
@Singleton
class SessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope applicationScope: CoroutineScope,
) {
    val session: StateFlow<Session?> = dataStore.data
        .map { preferences -> preferences.toSession() }
        // Un archivo corrupto no debe tumbar la app: equivale a no tener sesión.
        .catch { cause -> if (cause is IOException) emit(null) else throw cause }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    /** Lectura sincrónica para el interceptor, que corre fuera de una corrutina. */
    val currentSession: Session?
        get() = session.value

    /**
     * Espera a que DataStore termine de leer antes de decidir si hay sesión. Los
     * repositorios la usan antes de llamar a la API: así el interceptor ya tiene el token.
     */
    suspend fun awaitSession(): Session? = session.first()

    suspend fun save(session: Session) {
        dataStore.edit { preferences ->
            preferences[Keys.UserId] = session.userId
            preferences[Keys.Token] = session.token
            preferences[Keys.Role] = session.role.apiValue
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    private fun Preferences.toSession(): Session? {
        val userId = this[Keys.UserId] ?: return null
        val token = this[Keys.Token] ?: return null
        val role = this[Keys.Role]
            ?.let { value -> UserRole.entries.firstOrNull { it.apiValue == value } }
            ?: return null
        return Session(userId = userId, token = token, role = role)
    }

    private object Keys {
        val UserId = stringPreferencesKey("session_user_id")
        val Token = stringPreferencesKey("session_token")
        val Role = stringPreferencesKey("session_role")
    }
}
