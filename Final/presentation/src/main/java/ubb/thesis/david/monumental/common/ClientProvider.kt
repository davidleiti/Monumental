package ubb.thesis.david.monumental.common

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import ubb.thesis.david.domain.*

interface ClientProvider {
    fun getSignInClient(): GoogleSignInClient
    fun getBeaconManager(): BeaconManager
    fun getDataSource(): CloudDataSource
    fun getImageStorage(): ImageStorage
    fun getUserAuthenticator(): UserAuthenticator
    fun getLandmarkDetector(): LandmarkDetector
}