package pe.edu.upn.reciclatru.domain.repository.explore

import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptancesPage

interface MyAcceptancesRepository {
    suspend fun getMyAcceptances(tab: AcceptancesTab, cursor: String?): MyAcceptancesPage
}
