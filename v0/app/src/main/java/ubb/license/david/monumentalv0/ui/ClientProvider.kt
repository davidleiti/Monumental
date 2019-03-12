package ubb.license.david.monumentalv0.ui

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import ubb.license.david.monumentalv0.GeofencingClientWrapper

interface ClientProvider {
    fun getAuth(): FirebaseAuth
    fun getUserId(): String
    fun getApiClient(): GoogleApiClient
    fun getSignInClient(): GoogleSignInClient
    fun getGeofencingClient(): GeofencingClientWrapper
}