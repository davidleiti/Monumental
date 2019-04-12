package ubb.thesis.david.monumental.domain

interface BeaconManager {

    fun setupBeacon(id: String, lat: Double, lng: Double, collectionId: String)
    fun removeBeacons(collectionId: String)

}