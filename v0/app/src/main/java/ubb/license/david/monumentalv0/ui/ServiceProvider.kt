package ubb.license.david.monumentalv0.ui

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth

interface ServiceProvider {
    fun getAuth(): FirebaseAuth
    fun getGoogleApiClient(): GoogleApiClient
    fun getGoogleSignInClient(): GoogleSignInClient
}