package responses

import model.Photos
import model.Venue

sealed class ResponseBody

class PhotosResponseBody(val photos: Photos) : ResponseBody()

class VenueDetailsResponseBody(val venue: Venue) : ResponseBody()

class VenuesResponseBody(val venues: Array<Venue>) : ResponseBody()