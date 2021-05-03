package fi.oamk.chatapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val myDataset: ArrayList<String>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val myView = LayoutInflater.from(parent.context).
                inflate(R.layout.message_list,parent,false)
        return MyViewHolder(myView)
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
      holder.task.text = myDataset[position]
    }

    override fun getItemCount()= myDataset.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val task: TextView = itemView.findViewById(R.id.message)
}

}