package ubb.license.david.foursquareapi.responses

import ubb.license.david.foursquareapi.model.Photos
import ubb.license.david.foursquareapi.model.Venue

internal sealed class ResponseBody

internal class PhotosResponseBody(val photos: Photos) : ResponseBody()

internal class VenueDetailsResponseBody(val venue: Venue) : ResponseBody()

internal class VenuesResponseBody(val venues: Array<Venue>) : ResponseBody()