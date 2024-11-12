package com.gigzz.android.ui.jobs

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.databinding.ImageSliderComponentBinding
import com.gigzz.android.databinding.ItemUploadImagePagerBinding
import com.gigzz.android.domain.req.Medias
import com.gigzz.android.utils.show


private const val POST = 0
private const val JOB_POST = 1

class UploadPostViewPagerAdapter(
    private var mediaList: ArrayList<Medias>,
    val viewType: String,
    private val onItemClick: (pos: Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class PostViewHolder(var binding: ImageSliderComponentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItem: Medias) {
            try {
                binding.cross.show()
                binding.imageView.show()
                binding.imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                binding.cross.setOnClickListener {
                    onItemClick(absoluteAdapterPosition)
                }
                binding.imageView.setImageURI(Uri.fromFile(listItem.mediaUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class JobPostViewHolder(var binding: ItemUploadImagePagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItem: Medias) {

            binding.cross.setOnClickListener {
                onItemClick(absoluteAdapterPosition)
            }
//            binding.imageView.setImageURI(Uri.fromFile(listItem.mediaUrl))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            POST -> {
                val binding =
                    ImageSliderComponentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return PostViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemUploadImagePagerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return JobPostViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            POST -> (holder as PostViewHolder).bind(mediaList[position])
            JOB_POST -> (holder as JobPostViewHolder).bind(mediaList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (viewType == "post") POST
        else JOB_POST
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }
}