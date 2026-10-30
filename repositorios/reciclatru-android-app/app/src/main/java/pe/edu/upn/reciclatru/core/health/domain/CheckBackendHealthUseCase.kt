package pe.edu.upn.reciclatru.core.health.domain

import javax.inject.Inject

class CheckBackendHealthUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
) {
    suspend operator fun invoke(): Boolean = healthRepository.isBackendUp()
}
