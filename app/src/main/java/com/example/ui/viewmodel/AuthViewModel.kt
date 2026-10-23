package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.WorkerEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isRegisterMode: Boolean = false,
    val workerName: String = "",
    val workerCode: String = "",
    val workerRole: String = "Técnico Instalador FTTH",
    val assignedZone: String = "Zona Centro (OLT-CENTRO)",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            workerCode = "TEC-${(100..999).random()}"
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val database = AppDatabase.getDatabase(application, viewModelScope)

    fun onEmailChanged(newEmail: String) {
        _uiState.update { current ->
            current.copy(
                email = newEmail,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { current ->
            current.copy(
                password = newPassword,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                emailError = null,
                passwordError = null
            )
        }
    }

    fun onWorkerNameChanged(name: String) {
        _uiState.update { it.copy(workerName = name, errorMessage = null) }
    }

    fun onWorkerCodeChanged(code: String) {
        _uiState.update { it.copy(workerCode = code, errorMessage = null) }
    }

    fun onWorkerRoleSelected(role: String) {
        _uiState.update { it.copy(workerRole = role) }
    }

    fun onAssignedZoneSelected(zone: String) {
        _uiState.update { it.copy(assignedZone = zone) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, emailError = null, passwordError = null) }
    }

    fun setCustomError(error: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = error) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    /**
     * Valida los campos antes de ejecutar la acción de autenticación.
     */
    private fun validateInputs(isRegister: Boolean): Boolean {
        var isValid = true
        val state = _uiState.value
        val emailTrimmed = state.email.trim()

        var emailErr: String? = null
        if (emailTrimmed.isEmpty()) {
            emailErr = "Ingresa tu correo institucional o técnico"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            emailErr = "Formato de correo no válido (ej. usuario@fibertel.com)"
            isValid = false
        }

        var passwordErr: String? = null
        if (state.password.isEmpty()) {
            passwordErr = "Ingresa tu contraseña"
            isValid = false
        } else if (state.password.length < 6) {
            passwordErr = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        var generalErr: String? = null
        if (isRegister) {
            if (state.workerName.trim().isEmpty()) {
                generalErr = "Por favor ingresa el nombre completo del colaborador"
                isValid = false
            } else if (state.workerCode.trim().isEmpty()) {
                generalErr = "Por favor ingresa el código del colaborador"
                isValid = false
            }
        }

        _uiState.update {
            it.copy(
                emailError = emailErr,
                passwordError = passwordErr,
                errorMessage = generalErr
            )
        }

        return isValid
    }

    /**
     * Inicia sesión con correo electrónico y contraseña en Firebase Auth.
     */
    fun loginWithEmailPassword(onSuccess: (FirebaseUser) -> Unit) {
        if (!validateInputs(isRegister = false)) return

        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo recuperar el usuario de la sesión."
                        )
                    }
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                Log.w(TAG, "Usuario no encontrado", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No existe una cuenta registrada con este correo electrónico."
                    )
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w(TAG, "Credenciales inválidas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Contraseña incorrecta o correo inválido. Verifica tus datos."
                    )
                }
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Error de Firebase Auth: ${e.errorCode}", e)
                val msg = when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "El inicio de sesión por correo/contraseña no está activo en Firebase. Utiliza 'Continuar con Google' para acceder."
                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Demasiados intentos fallidos. Inténtalo de nuevo más tarde."
                    else -> e.localizedMessage ?: "Error al autenticar con el servidor de Firebase."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción general de login", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error de conexión al iniciar sesión."
                    )
                }
            }
        }
    }

    /**
     * Registra un nuevo colaborador con correo electrónico y contraseña,
     * guardando además su perfil en la base de datos de trabajadores.
     */
    fun registerWithEmailPassword(onSuccess: (FirebaseUser) -> Unit) {
        if (!validateInputs(isRegister = true)) return

        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password
        val workerName = state.workerName.trim()
        val workerCode = state.workerCode.trim()
        val workerRole = state.workerRole
        val assignedZone = state.assignedZone

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    // Actualizar el nombre en Firebase Auth
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = workerName
                        }
                        user.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo actualizar el nombre en Firebase Auth", e)
                    }

                    // Guardar registro de colaborador en la base de datos local / CRM
                    try {
                        database.workerDao().insertWorker(
                            WorkerEntity(
                                fullName = workerName,
                                email = email,
                                role = workerRole,
                                assignedZone = assignedZone,
                                employeeNumber = workerCode,
                                functionsCsv = com.example.data.model.WorkerFunctions.getDefaultFunctionsForRole(workerRole).joinToString(",")
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo insertar trabajador en BD local", e)
                    }

                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo completar el registro del usuario."
                        )
                    }
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.w(TAG, "Usuario colisiona", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ya existe un colaborador registrado con este correo."
                    )
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.w(TAG, "Contraseña débil", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "La contraseña es muy débil. Debe tener al menos 6 caracteres seguros."
                    )
                }
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Error de Firebase Auth en registro: ${e.errorCode}", e)
                val msg = when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "El registro por correo/contraseña no está activo en Firebase. Puedes iniciar sesión con Google."
                    else -> e.localizedMessage ?: "Error al registrar la cuenta en Firebase."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción general en registro", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error de conexión al registrar la cuenta."
                    )
                }
            }
        }
    }

    /**
     * Autenticación con credencial de Google IdToken procesada mediante CredentialManager.
     */
    fun loginWithGoogleIdToken(idToken: String, onSuccess: (FirebaseUser) -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener el usuario autenticado de Google."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error autenticando con Google en ViewModel", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error al validar la sesión con Google."
                    )
                }
            }
        }
    }
}
