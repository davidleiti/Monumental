package ubb.thesis.david.monumental.common

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.monumental.GeofencingClientAdapter

interface ClientProvider {
    fun getAuth(): FirebaseAuth
    fun getUserId(): String
    fun getApiClient(): GoogleApiClient
    fun getSignInClient(): GoogleSignInClient
    fun getGeofencingClient(): BeaconManager
}