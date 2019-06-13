package ubb.thesis.david.data.common

import ubb.license.david.foursquareapi.model.Location
import ubb.license.david.foursquareapi.model.Venue
import ubb.thesis.david.domain.common.Mapper
import ubb.thesis.david.domain.entities.Landmark

class LandmarkMapper : Mapper<Venue, Landmark>() {
    override fun mapFrom(obj: Venue): Landmark =
        Landmark(id = obj.id,
                 lat = obj.location.lat.toDouble(),
                 lng = obj.location.lng.toDouble(),
                 label = obj.name)

    override fun mapTo(obj: Landmark): Venue =
        Venue(id = obj.id,
              name = if (obj.label != null) obj.label!! else "Unknown",
              location = Location(lat = obj.lat.toString(), lng = obj.lng.toString()))
}