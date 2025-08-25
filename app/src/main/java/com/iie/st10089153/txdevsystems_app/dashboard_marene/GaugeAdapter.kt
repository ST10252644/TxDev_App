package dashboard_marene

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import dashboard_marene.models.GaugeCard

class GaugeAdapter(private val items: List<dashboard_marene.models.GaugeCard>) :
    RecyclerView.Adapter<GaugeAdapter.GaugeViewHolder>() {

    class GaugeViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.icon_view)
        val statusText: TextView = view.findViewById(R.id.status_text)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val measurementText: TextView = view.findViewById(R.id.measurement_text)
        val gaugeContainer: FrameLayout = view.findViewById(R.id.gauge_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaugeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gauge_card, parent, false)
        return GaugeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GaugeViewHolder, position: Int) {
        val item = items[position]

        holder.iconView.setImageResource(item.iconRes)
        holder.statusText.text = item.statusText
        holder.nameText.text = item.name

        if (item.measurement.isNullOrEmpty()) {
            holder.measurementText.visibility = View.INVISIBLE // hides “Volts” if not needed
        } else {
            holder.measurementText.text = item.measurement
        }

        holder.gaugeContainer.background =
            ContextCompat.getDrawable(holder.view.context, item.gaugeImageRes)
    }

    override fun getItemCount(): Int = items.size
}
