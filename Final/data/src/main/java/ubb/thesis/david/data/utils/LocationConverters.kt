package ubb.thesis.david.data.utils

import android.location.Location
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import ubb.thesis.david.domain.entities.Landmark

fun FirebaseVisionCloudLandmark.toLocation(): Location =
    Location("").apply {
        latitude = locations[0].latitude
        longitude = locations[0].longitude
    }

fun Landmark.toLocation(): Location =
    Location("").apply {
        latitude = lat
        longitude = lng
    }
