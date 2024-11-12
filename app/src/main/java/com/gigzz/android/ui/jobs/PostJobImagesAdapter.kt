package com.gigzz.android.ui.jobs

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.databinding.AddLayoutBinding
import com.gigzz.android.databinding.ImageSliderComponentBinding
import com.gigzz.android.databinding.PostJobImageLayoutBinding
import com.gigzz.android.domain.req.Medias
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import java.util.*

class PostJobImagesAdapter(
    val context: Context,
    private var playlist: ArrayList<Medias>,
    private val onItemClick: (pos: Int, model: Medias?, src: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var TYPE_ADD = 0
    private var TYPE_PLAN = 1

    inner class AddViewHolder(var binding:AddLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            itemView.remove()
           // if (playlist.size<5) itemView.show() else itemView.remove()
            itemView.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition,null,"root")
            }
        }

    }

    inner class PlanViewHolder(val binding: PostJobImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItem: Medias) {
            try {
                binding.cross.show()
                binding.imageView.show()
                binding.imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                binding.cross.setOnClickListener {
                    onItemClick(absoluteAdapterPosition, listItem, "cross")
                }
                binding.imageView.setImageURI(Uri.fromFile(listItem.mediaUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == playlist.size) TYPE_ADD else TYPE_PLAN
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PLAN -> {
                val binding =
                    PostJobImageLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return PlanViewHolder(binding)
            }
            else -> {
                val binding =
                    AddLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return AddViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_PLAN -> {
                (holder as PlanViewHolder).bind(playlist[position])
            }

            TYPE_ADD -> {
                (holder as AddViewHolder).bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return playlist.size+1
    }
}


