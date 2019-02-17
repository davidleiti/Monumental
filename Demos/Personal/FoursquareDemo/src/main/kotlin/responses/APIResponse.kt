package responses

import model.Meta

sealed class APIResponse(val meta: Meta)

class VenuesResponse(meta: Meta, val response: VenuesResponseBody) : APIResponse(meta)

class VenueDetailsResponse(meta: Meta, val response: VenueDetailsResponseBody) : APIResponse(meta)

class PhotosResponse(meta: Meta, val response: PhotosResponseBody) : APIResponse(meta)