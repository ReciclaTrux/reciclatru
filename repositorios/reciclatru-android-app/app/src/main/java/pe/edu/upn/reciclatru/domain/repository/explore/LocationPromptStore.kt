package pe.edu.upn.reciclatru.domain.repository.explore

interface LocationPromptStore {
    fun wasShown(): Boolean
    fun markShown()
}
