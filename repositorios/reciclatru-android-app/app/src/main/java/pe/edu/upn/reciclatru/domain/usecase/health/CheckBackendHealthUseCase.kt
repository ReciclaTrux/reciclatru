package pe.edu.upn.reciclatru.domain.usecase.health

import pe.edu.upn.reciclatru.domain.repository.health.HealthRepository
import javax.inject.Inject

class CheckBackendHealthUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
) {
    suspend operator fun invoke(): Boolean = healthRepository.isBackendUp()
}
