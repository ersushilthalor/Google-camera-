package com.example.camera.model

enum class CinematicLut(val id: String, val displayName: String, val colorDescription: String) {
    NATURAL("natural", "Natural (Rec.709)", "Faithful color representation with clean skin tones"),
    WARM_GOLDEN("warm_golden", "Golden Hour", "Rich warm amber highlights with golden glow"),
    TEAL_ORANGE("teal_orange", "Teal & Orange", "Blockbuster movie contrast with complementary tones"),
    MOODY_NOIR("moody_noir", "Moody Noir", "Deep rich shadows and lowered midtone luminance"),
    VINTAGE_FILM("vintage_film", "Vintage 35mm", "Subtle film grain simulation and pastel greens"),
    MATRIX_EMERALD("matrix_emerald", "Emerald Sci-Fi", "Futuristic stylized green-tinted shadow grade"),
    CUSTOM_CUBE("custom_cube", "Custom .CUBE", "User imported 3D LUT profile")
}
