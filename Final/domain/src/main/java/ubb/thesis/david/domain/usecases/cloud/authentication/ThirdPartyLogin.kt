package ubb.thesis.david.domain.usecases.cloud.authentication

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.entities.Credential
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class ThirdPartyLogin(private val credential: Credential,
                      private val userAuthenticator: UserAuthenticator,
                      transformer: CompletableTransformer): CompletableUseCase(transformer){

    override fun createSource(): Completable =
            userAuthenticator.thirdPartyAuth(credential)

}