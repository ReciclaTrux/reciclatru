package pe.edu.upn.reciclatru.feature.explore.domain

interface LocationPromptStore {
    fun wasShown(): Boolean
    fun markShown()
}
