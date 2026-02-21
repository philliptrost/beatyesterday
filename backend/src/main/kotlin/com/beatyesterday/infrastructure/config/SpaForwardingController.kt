package com.beatyesterday.infrastructure.config

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Catches all non-API, non-static requests and forwards them to index.html
 * so React Router can handle client-side routing. Only relevant in production
 * when the SPA is served from Spring Boot's static resources.
 */
@Controller
class SpaForwardingController {

    @RequestMapping(
        value = [
            "/{path:[^\\.]*}",
            "/{path:[^\\.]*}/**"
        ]
    )
    fun forward(): String {
        return "forward:/index.html"
    }
}
