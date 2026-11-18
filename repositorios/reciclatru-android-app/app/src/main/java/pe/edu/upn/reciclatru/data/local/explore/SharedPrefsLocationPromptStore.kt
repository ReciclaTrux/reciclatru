package pe.edu.upn.reciclatru.data.local.explore

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upn.reciclatru.domain.repository.explore.LocationPromptStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsLocationPromptStore @Inject constructor(
    @ApplicationContext context: Context,
) : LocationPromptStore {

    private val preferences = context.getSharedPreferences(FileName, Context.MODE_PRIVATE)

    override fun wasShown(): Boolean = preferences.getBoolean(ShownKey, false)

    override fun markShown() {
        preferences.edit().putBoolean(ShownKey, true).apply()
    }

    private companion object {
        const val FileName = "explore"
        const val ShownKey = "location_prompt_shown"
    }
}
