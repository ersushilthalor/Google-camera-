package com.example.camera.model

enum class PhotoFilter(val id: String, val displayName: String, val description: String) {
    ORIGINAL("original", "Original", "Natural sensor color"),
    WARM_SUNSET("warm_sunset", "Warm Sunset", "Golden amber highlights with rich skin tones"),
    VIVID("vivid", "Vivid", "Enhanced color saturation and dynamic vibrance"),
    COOL_NOIR("cool_noir", "Cool Noir", "Subtle blue shadows with classic cinematic contrast"),
    MONOCHROME("monochrome", "B&W Film", "Classic black and white with high tonal range"),
    VINTAGE_FILM("vintage_film", "Vintage 35mm", "Retro analog look with lifted greens"),
    EMERALD_MINT("emerald_mint", "Emerald", "Modern stylized cyan-green cast"),
    PASTEL_SOFT("pastel_soft", "Pastel", "Dreamy soft highlights and gentle tones")
}
