package ubb.thesis.david.domain

interface BeaconManager {

    fun setupBeacon(id: String, lat: Double, lng: Double, collectionId: String)
    fun removeBeacon(id: String, collectionId: String)
    fun wipeBeacons(collectionId: String)

}