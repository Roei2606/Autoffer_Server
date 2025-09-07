package org.autoffer.mappers

import org.autoffer.models.AlumProfileModel
import org.autoffer.models.AlumProfileModelDTO
import org.autoffer.models.GlassModel
import org.autoffer.models.GlassModelDTO


fun AlumProfileModel.toDTO(): AlumProfileModelDTO = AlumProfileModelDTO(
    profileNumber = this.profileNumber,
    usageType = this.usageType,
    pricePerSquareMeter = this.pricePerSquareMeter
)
fun GlassModel.toDTO(): GlassModelDTO = GlassModelDTO(
    type = this.type,
    pricePerSquareMeter = this.pricePerSquareMeter
)


