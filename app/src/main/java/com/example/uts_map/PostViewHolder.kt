import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_map.R

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.tvTitle)

    fun bind(post: Post) {
        title.text = post.title
    }
}
