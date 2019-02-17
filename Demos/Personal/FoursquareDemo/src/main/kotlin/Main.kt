import model.Photo
import model.Tip
import responses.VenuesResponse

fun main(args: Array<String>) {
    val service = FoursquareAPI.instance

    val locationCluj = "46.7709,23.5899"
    val radius = 50000
    val categories =
        "4bf58dd8d48988d181941735,52e81612bcbc57f1066b79ed,50aaa49e4b90af0d42d5de11,4bf58dd8d48988d12d941735"
    val venueId = "4f60d7f2e4b006673b1e8db5"

    val detailsResponse = service.fetchDetails(venueId).execute()
    val photosResponse = service.fetchPhotos(venueId).execute()

    detailsResponse.body()?.let {
        println("Details: ${it.response.venue}")
        println("Tips: ${it.response.venue.tips!!.groups[0].items}")
    }

    photosResponse.body()?.let {
        println("Photos:")
        for (photo: Photo in it.response.photos.items) {
            println("\t${photo.generateUrl()}")
        }
    }
}
