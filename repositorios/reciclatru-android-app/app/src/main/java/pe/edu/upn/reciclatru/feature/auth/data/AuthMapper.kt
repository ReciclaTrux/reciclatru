package pe.edu.upn.reciclatru.feature.auth.data

import pe.edu.upn.reciclatru.core.session.Session
import pe.edu.upn.reciclatru.core.session.UserRole

fun PartyAuthenticationOutputDto.toSession(): Session? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val userId = (crReferenceId ?: record?.id)?.takeIf { it.isNotBlank() } ?: return null
    val role = record?.role?.let { value -> UserRole.entries.firstOrNull { it.apiValue == value } }
        ?: return null
    return Session(userId = userId, token = token, role = role)
}
