package ubb.thesis.david.data.adapters

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.entities.Credential

class FirebaseAuthenticatorAdapter : UserAuthenticator {

    private val authenticatorClient = FirebaseAuth.getInstance()

    override fun emailAuth(email: String, password: String): Completable {
        val authTask = CompletableSubject.create()

        authenticatorClient.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    info(TAG_LOG, "Authentication with email/password has been successful.")
                    authTask.onComplete()
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Authentication with email/password has failed with error ${error.message}")
                    authTask.onError(error)
                }

        return authTask
    }

    override fun emailSignUp(email: String, password: String): Completable {
        val signUpTask = CompletableSubject.create()

        authenticatorClient.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { user ->
                    info(TAG_LOG, "User $user has been created successfully!")
                    signUpTask.onComplete()
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to create user with error ${error.message}")
                    signUpTask.onError(error)
                }

        return signUpTask
    }

    override fun thirdPartyAuth(credential: Credential): Completable {
        val authTask = CompletableSubject.create()

        val authCredentials = (credential.getCredentials() as? AuthCredential)

        authCredentials?.let {
            authenticatorClient.signInWithCredential(it)
                    .addOnSuccessListener { _ ->
                        info(TAG_LOG, "Authentication via ${it.provider} has been successful!")
                        authTask.onComplete()
                    }.addOnFailureListener { error ->
                        debug(TAG_LOG, "Authentication via ${it.provider} has failed with error ${error.message}")
                        authTask.onError(error)
                    }
        } ?: run {
            authTask.onError(RuntimeException("Invalid credential type has been provided! " +
                                                      "Authenticator needs credential of type ${AuthCredential::class.java.name}"))
        }

        return authTask
    }

    companion object {
        private const val TAG_LOG = "FirebaseAuthenticatorLogger"
    }

}