package ubb.license.david.foursquareapi.responses

import ubb.license.david.foursquareapi.model.ExploreGroup
import ubb.license.david.foursquareapi.model.Photos
import ubb.license.david.foursquareapi.model.Venue

internal sealed class ResponseBody {

    class PhotosBody(val photos: Photos) : ResponseBody()

    class VenueDetailsBody(val venue: Venue) : ResponseBody()

    class SearchVenuesBody(val venues: List<Venue>) : ResponseBody()

    class ExploreVenuesBody(val groups: List<ExploreGroup>) : ResponseBody()

}