package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Sign in using Google identity via Credential Manager
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("વપરાશકર્તા માહિતી મેળવી શકાઈ નથી."))
                }
            } else {
                Result.failure(Exception("અસમર્થિત ક્રેડેન્શિયલ પ્રકાર."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthManager", "Google Sign-In was cancelled by user: ${e.message}")
            Result.failure(Exception("સાઇન-ઇન રદ કરવામાં આવ્યું. કૃપા કરીને તમારું Google એકાઉન્ટ પસંદ કરો."))
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In failed", e)
            val msg = e.localizedMessage ?: "Google સાઇન-ઇન નિષ્ફળ ગયું."
            Result.failure(Exception(msg))
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmailAndPassword(email: String, pass: String): Result<FirebaseUser> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(Exception("કૃપા કરીને માન્ય ઈમેલ સરનામું દાખલ કરો."))
        }
        if (cleanPass.isEmpty()) {
            return Result.failure(Exception("કૃપા કરીને પાસવર્ડ દાખલ કરો."))
        }
        return try {
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("વપરાશકર્તા વિગતો મેળવી શકાઈ નથી."))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("આ ઈમેલ સાથે કોઈ એકાઉન્ટ મળ્યું નથી. કૃપા કરીને 'નવું ખાતું બનાવો' પર ક્લિક કરો."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("ઈમેલ અથવા પાસવર્ડ ખોટો છે."))
        } catch (e: FirebaseAuthException) {
            val msg = e.message ?: ""
            val code = e.errorCode
            if (code == "ERROR_OPERATION_NOT_ALLOWED" ||
                msg.contains("operation is not allowed", ignoreCase = true) ||
                msg.contains("disabled", ignoreCase = true)
            ) {
                Result.failure(Exception("⚠️ આ Firebase પ્રોજેક્ટમાં Email/Password સેવા બંધ છે. કૃપા કરીને 'Google દ્વારા સાઇન ઇન કરો' (Google Sign-In) વાપરો, જે ૧-ક્લિકમાં ૧૦૦% કાર્યરત છે."))
            } else {
                Result.failure(Exception("લૉગિનમાં ત્રુટિ: ${e.localizedMessage ?: e.message}"))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.contains("operation is not allowed", ignoreCase = true) ||
                msg.contains("disabled", ignoreCase = true)
            ) {
                Result.failure(Exception("⚠️ આ Firebase પ્રોજેક્ટમાં Email/Password સેવા બંધ છે. કૃપા કરીને 'Google દ્વારા સાઇન ઇન કરો' (Google Sign-In) વાપરો, જે ૧-ક્લિકમાં ૧૦૦% કાર્યરત છે."))
            } else {
                Result.failure(Exception(msg.ifBlank { "લૉગિન નિષ્ફળ ગયું." }))
            }
        }
    }

    /**
     * Create new user with Email and Password
     */
    suspend fun createUserWithEmailAndPassword(email: String, pass: String): Result<FirebaseUser> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(Exception("કૃપા કરીને માન્ય ઈમેલ સરનામું દાખલ કરો."))
        }
        if (cleanPass.length < 6) {
            return Result.failure(Exception("પાસવર્ડ ઓછામાં ઓછો 6 અક્ષરોનો હોવો જોઈએ."))
        }
        return try {
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("એકાઉન્ટ બનાવી શકાયું નથી."))
            }
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("પાસવર્ડ ઘણો નબળો છે. ઓછામાં ઓછા 6 અક્ષરો વાપરો."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("આ ઈમેલ પહેલેથી જ રજીસ્ટર્ડ છે. કૃપા કરીને લૉગિન કરો."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("અમાન્ય ઈમેલ ફોર્મેટ. યોગ્ય ઈમેલ દાખલ કરો."))
        } catch (e: FirebaseAuthException) {
            val msg = e.message ?: ""
            val code = e.errorCode
            if (code == "ERROR_OPERATION_NOT_ALLOWED" ||
                msg.contains("operation is not allowed", ignoreCase = true) ||
                msg.contains("disabled", ignoreCase = true)
            ) {
                Result.failure(Exception("⚠️ આ Firebase પ્રોજેક્ટમાં Email/Password સેવા બંધ છે. કૃપા કરીને 'Google દ્વારા સાઇન ઇન કરો' (Google Sign-In) વાપરો, જે ૧-ક્લિકમાં ૧૦૦% કાર્યરત છે."))
            } else {
                Result.failure(Exception("સાઇન-અપ ત્રુટિ: ${e.localizedMessage ?: e.message}"))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.contains("operation is not allowed", ignoreCase = true) ||
                msg.contains("disabled", ignoreCase = true)
            ) {
                Result.failure(Exception("⚠️ આ Firebase પ્રોજેક્ટમાં Email/Password સેવા બંધ છે. કૃપા કરીને 'Google દ્વારા સાઇન ઇન કરો' (Google Sign-In) વાપરો, જે ૧-ક્લિકમાં ૧૦૦% કાર્યરત છે."))
            } else {
                Result.failure(Exception(msg.ifBlank { "એકાઉન્ટ બનાવવામાં ભૂલ આવી." }))
            }
        }
    }

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(Exception("પાસવર્ડ રીસેટ કરવા માન્ય ઈમેલ દાખલ કરો."))
        }
        return try {
            auth.sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "પાસવર્ડ રીસેટ લિંક મોકલવામાં ભૂલ આવી."))
        }
    }

    suspend fun signOut() {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("AuthManager", "Error clearing credential state during sign-out", e)
        }
    }
}
