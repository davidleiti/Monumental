package ubb.thesis.david.domain

import io.reactivex.Completable
import ubb.thesis.david.domain.entities.Credential

interface UserAuthenticator {

    fun thirdPartyAuth(credential: Credential): Completable
    fun emailAuth(email: String, password: String): Completable
    fun emailSignUp(email: String, password: String): Completable

}