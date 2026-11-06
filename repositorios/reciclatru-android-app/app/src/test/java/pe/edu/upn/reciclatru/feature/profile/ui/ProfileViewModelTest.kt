package pe.edu.upn.reciclatru.feature.profile.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.edu.upn.reciclatru.MainDispatcherRule
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.profile.domain.GetProfileUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.feature.profile.domain.UpdateProfileUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfile
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfileRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `carga el perfil al abrir`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeUserProfileRepository(profile = neighbor(fullName = "Ana Torres")))
        assertTrue(viewModel.uiState.value.loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Ana Torres", state.fullName)
        assertEquals(UserRole.Neighbor, state.role)
    }

    @Test
    fun `si falla la carga muestra el error y permite reintentar`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository(
            getError = NetworkException(NetworkErrorKind.NoConnection, "No tienes conexión. Revisa tu red e inténtalo de nuevo."),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.loadError)

        repository.getError = null
        viewModel.onRetry()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadError)
        assertEquals(2, repository.getCalls)
    }

    @Test
    fun `celular invalido no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFullNameChange("Ana Torres")
        viewModel.onPhoneChange("94812")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals("Debe tener 9 dígitos y empezar con 9", viewModel.uiState.value.phoneError)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun `nombre vacio no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onPhoneChange("987654321")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals("Ingresa tu nombre y apellido.", viewModel.uiState.value.fullNameError)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun `el celular solo acepta 9 digitos`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeUserProfileRepository())
        advanceUntilIdle()

        viewModel.onPhoneChange("987 654 321 00")

        assertEquals("987654321", viewModel.uiState.value.phone)
    }

    @Test
    fun `vecino guarda nombre, celular y referencia`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFullNameChange(" Ana Torres ")
        viewModel.onPhoneChange("948120337")
        viewModel.onReferenceChange("Dpto. 302")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals("Ana Torres", repository.lastFullName)
        assertEquals("948120337", repository.lastPhone)
        assertEquals("Dpto. 302", repository.lastReference)
        assertTrue(viewModel.uiState.value.savedOnce)
        assertEquals("Guardamos tu perfil.", viewModel.uiState.value.message)
    }

    @Test
    fun `reciclador no envia referencia`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository(profile = neighbor().copy(role = UserRole.Recycler))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFullNameChange("Juan Pérez")
        viewModel.onPhoneChange("987654321")
        viewModel.onSave()
        advanceUntilIdle()

        assertNull(repository.lastReference)
        assertTrue(viewModel.uiState.value.savedOnce)
    }

    @Test
    fun `si falla el guardado conserva lo escrito`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository(
            updateError = NetworkException(NetworkErrorKind.Unknown, "No pudimos completar la operación.", 400),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFullNameChange("Ana Torres")
        viewModel.onPhoneChange("948120337")
        viewModel.onSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Revisa tus datos e inténtalo de nuevo.", state.saveError)
        assertEquals("Ana Torres", state.fullName)
        assertFalse(state.savedOnce)
    }

    @Test
    fun `al volver del mapa actualiza solo el punto de recojo`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onFullNameChange("Ana sin guardar")

        repository.profile = repository.profile.copy(
            pickupAddress = PickupAddress(address = "Av. Larco 1250", district = "Víctor Larco Herrera"),
        )
        viewModel.onReturnFromPickupPoint()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Av. Larco 1250", state.pickupAddress?.address)
        assertEquals("Ana sin guardar", state.fullName)
    }

    private fun neighbor(fullName: String? = null) = UserProfile(
        email = "ana.torres@gmail.com",
        role = UserRole.Neighbor,
        fullName = fullName,
        phone = null,
        reference = null,
        pickupAddress = null,
        profileComplete = false,
    )

    private fun createViewModel(repository: UserProfileRepository) = ProfileViewModel(
        getProfile = GetProfileUseCase(repository),
        updateProfile = UpdateProfileUseCase(repository),
    )

    private inner class FakeUserProfileRepository(
        var profile: UserProfile = neighbor(),
        var getError: Exception? = null,
        var updateError: Exception? = null,
    ) : UserProfileRepository {
        var getCalls = 0
        var updateCalls = 0
        var lastFullName: String? = null
        var lastPhone: String? = null
        var lastReference: String? = null

        override suspend fun getProfile(): UserProfile {
            getCalls++
            getError?.let { throw it }
            return profile
        }

        override suspend fun updateProfile(fullName: String, phone: String, reference: String?): UserProfile {
            updateCalls++
            lastFullName = fullName
            lastPhone = phone
            lastReference = reference
            updateError?.let { throw it }
            return profile.copy(fullName = fullName, phone = phone, reference = reference)
        }
    }
}
