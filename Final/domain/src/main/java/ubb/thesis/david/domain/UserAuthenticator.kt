package ubb.thesis.david.domain

import io.reactivex.Completable
import ubb.thesis.david.domain.entities.Credentials

interface UserAuthenticator {

    fun thirdPartyAuth(credentials: Credentials): Completable
    fun emailAuth(email: String, password: String): Completable
    fun emailSignUp(email: String, password: String): Completable

}