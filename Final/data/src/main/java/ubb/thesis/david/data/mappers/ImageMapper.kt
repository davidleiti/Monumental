package ubb.thesis.david.data.mappers

import ubb.thesis.david.domain.entities.ImageEntity
import ubb.license.david.foursquareapi.model.Photo
import ubb.thesis.david.domain.common.Mapper

class ImageMapper : Mapper<Photo, ImageEntity>() {

    override fun mapFrom(obj: Photo): ImageEntity =
        ImageEntity(id = obj.id,
                    width = obj.width.toInt(),
                    height = obj.height.toInt(),
                    url = obj.url())

    override fun mapTo(obj: ImageEntity): Photo =
            Photo(id = obj.id,
                  width =  obj.width.toString(),
                  height = obj.height.toString())
}