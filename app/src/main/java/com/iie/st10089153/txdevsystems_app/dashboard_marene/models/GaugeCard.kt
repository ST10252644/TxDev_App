package dashboard_marene.models

data class GaugeCard(
    val iconRes: Int,
    val statusText: String,
    val name: String,
    val measurement: String? = null,
    val gaugeImageRes: Int
)