package pe.edu.upn.reciclatru.feature.auth.domain

import pe.edu.upn.reciclatru.core.session.Session

data class LoginResult(
    val session: Session,
    val profileComplete: Boolean,
)
