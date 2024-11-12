package com.gigzz.android.ui.home

import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ImageSliderComponentBinding
import com.gigzz.android.domain.res.MediaItem
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show

class FeedViewPagerAdapter(
    private var mediaList: List<MediaItem?>,
    private val onItemClick: (root: String, model: MediaItem?, binding: ImageSliderComponentBinding) -> Unit,
) : RecyclerView.Adapter<FeedViewPagerAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding: ImageSliderComponentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(media: MediaItem?) {

            binding.cross.remove()
            if (media?.mediaType != "V") {
                binding.imageView.show()
                if (media?.imageUrl.isNullOrEmpty()) {
                    media?.cacheMediaUrl = binding.imageView.loadCachedImg(
                        media?.mediaUrl, media?.cacheMediaUrl,
                        R.drawable.post_placeholder
                    )
                    binding.imageView.setOnClickListener {
                        onItemClick("PostDetails", media, binding)
                    }
                } else {
                    media?.cacheMediaUrl = binding.imageView.loadCachedImg(
                        media?.imageUrl, media?.cacheMediaUrl,
                        R.drawable.post_placeholder
                    )
                }
            } else {
                binding.imageView.show()
                binding.playPauseBtn.show()
                binding.imageView
                media.cacheMediaUrl = binding.imageView.loadCachedImg(
                    media.mediaThumbnail, media.cacheMediaUrl,
                    R.drawable.post_placeholder
                )
                binding.playPauseBtn.setOnClickListener {
                    onItemClick("Player", media, binding)
                }
                binding.speakerImg.setOnClickListener {
                    onItemClick("postVolume", media, binding)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FeedViewPagerAdapter.MyViewHolder {

        val binding = ImageSliderComponentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewPagerAdapter.MyViewHolder, position: Int) {
        holder.bind(mediaList[position])

    }

    override fun getItemCount(): Int {
        return mediaList.size
    }
}