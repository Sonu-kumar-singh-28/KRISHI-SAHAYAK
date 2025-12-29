package com.ssu.xyvento.sihapp.dataclass

data class MarketPriceResponse(
    val records: List<MarketPrice>
)

data class MarketPrice(
    val commodity: String,
    val state: String,
    val district: String,
    val market: String,
    val min_price: String,
    val max_price: String,
    val modal_price: String,
    val arrival_date: String
)
