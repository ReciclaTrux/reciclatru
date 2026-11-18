package pe.edu.upn.reciclatru.model.auth

import pe.edu.upn.reciclatru.model.session.Session

data class LoginResult(
    val session: Session,
    val profileComplete: Boolean,
)
