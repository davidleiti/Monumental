package ubb.license.david.foursquareapi.responses

import com.google.gson.annotations.SerializedName
import ubb.license.david.foursquareapi.model.Meta

internal sealed class APIResponse(val meta: Meta)

internal class VenuesResponse(meta: Meta,
                              @SerializedName("response")
                              val body: VenuesResponseBody) : APIResponse(meta)

internal class VenueDetailsResponse(meta: Meta,
                                    @SerializedName("response")
                                    val body: VenueDetailsResponseBody) : APIResponse(meta)

internal class PhotosResponse(meta: Meta,
                              @SerializedName("response")
                              val body: PhotosResponseBody) : APIResponse(meta)