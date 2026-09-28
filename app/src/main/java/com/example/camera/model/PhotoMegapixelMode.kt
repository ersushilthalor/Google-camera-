package com.example.camera.model

enum class PhotoMegapixelMode(val label: String, val title: String, val targetWidth: Int, val targetHeight: Int) {
    STANDARD_12MP("12MP", "12 MP (Fast & Low Light)", 4000, 3000),
    ULTRA_RES_50MP("50MP", "50 MP Ultra Clarity (Multi-frame Stacking)", 8160, 6120)
}
