package ubb.license.david.foursquareapi.responses

import com.google.gson.annotations.SerializedName
import ubb.license.david.foursquareapi.model.Meta

internal sealed class ApiResponse(val meta: Meta) {

    class VenuesResponse(meta: Meta,
                         @SerializedName("response")
                         val body: ResponseBody.VenuesBody) : ApiResponse(meta)

    class VenueDetailsResponse(meta: Meta,
                               @SerializedName("response")
                               val body: ResponseBody.VenueDetailsBody) : ApiResponse(meta)

    class PhotosResponse(meta: Meta,
                         @SerializedName("response")
                         val body: ResponseBody.PhotosBody) : ApiResponse(meta)

}