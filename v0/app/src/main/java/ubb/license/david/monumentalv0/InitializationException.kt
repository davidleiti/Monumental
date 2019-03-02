package ubb.license.david.monumentalv0

import java.lang.RuntimeException

class InitializationException(message: String): RuntimeException(message) {
    constructor(): this("")
}