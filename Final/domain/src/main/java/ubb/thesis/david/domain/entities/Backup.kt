package ubb.thesis.david.domain.entities

data class Backup(val session: Session, val landmarks: Map<Landmark, Discovery?>)