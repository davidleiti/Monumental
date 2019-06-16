package ubb.thesis.david.data.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session

fun Landmark.asDataMapping(): MutableMap<String, Any?> =
    hashMapOf("lat" to lat,
              "lng" to lng,
              "label" to label,
              "foundAt" to null,
              "photoId" to null)

fun Session.asDataMapping(): Map<String, Any?> =
    hashMapOf("userId" to userId,
              "timeStarted" to timeStarted,
              "timeFinished" to timeFinished,
              "landmarkCount" to landmarkCount)

fun DocumentSnapshot.extractLandmarkData(): Pair<Landmark, Discovery?> {
    val landmark = Landmark(
            id = id,
            label = this["label"] as String,
            lat = this["lat"] as Double,
            lng = this["lng"] as Double)

    val foundAt = (this["foundAt"] as? Timestamp)?.toDate()
    val photoId = this["photoId"] as? String

    if (foundAt != null && photoId != null)
        return Pair(landmark, Discovery(foundAt, photoId))

    return Pair(landmark, null)
}

fun DocumentSnapshot.extractSessionData(): Session =
    Session(userId = this["userId"] as String,
            sessionId = id,
            landmarkCount = (this["landmarkCount"] as Long).toInt(),
            timeStarted = (this["timeStarted"] as Timestamp).toDate(),
            timeFinished = (this["timeFinished"] as? Timestamp)?.toDate()
    )