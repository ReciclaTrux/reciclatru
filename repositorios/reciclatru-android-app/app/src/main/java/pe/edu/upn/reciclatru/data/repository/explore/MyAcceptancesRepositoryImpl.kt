package pe.edu.upn.reciclatru.data.repository.explore

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.remote.api.explore.ExploreApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.mapper.explore.toMyAcceptancesPage
import pe.edu.upn.reciclatru.data.remote.mapper.explore.toQuery
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.explore.MyAcceptancesRepository
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptancesPage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyAcceptancesRepositoryImpl @Inject constructor(
    private val api: ExploreApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MyAcceptancesRepository {

    override suspend fun getMyAcceptances(tab: AcceptancesTab, cursor: String?): MyAcceptancesPage =
        withContext(ioDispatcher) {
            apiCall { api.getMyAcceptances(tab = tab.toQuery(), limit = PageSize, cursor = cursor) }
                .getOrElse { error -> throw error }
                .toMyAcceptancesPage()
        }

    private companion object {
        const val PageSize = 20
    }
}
