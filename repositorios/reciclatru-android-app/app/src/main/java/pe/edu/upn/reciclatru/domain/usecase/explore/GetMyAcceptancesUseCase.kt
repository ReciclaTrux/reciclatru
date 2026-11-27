package pe.edu.upn.reciclatru.domain.usecase.explore

import pe.edu.upn.reciclatru.domain.repository.explore.MyAcceptancesRepository
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptancesPage
import javax.inject.Inject

class GetMyAcceptancesUseCase @Inject constructor(
    private val repository: MyAcceptancesRepository,
) {
    suspend operator fun invoke(tab: AcceptancesTab, cursor: String? = null): MyAcceptancesPage =
        repository.getMyAcceptances(tab, cursor)
}
