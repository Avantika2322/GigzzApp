package com.gigzz.android.ui.home

import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentVideoPlayerBinding
import com.gigzz.android.domain.res.MediaItem
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.VolumeState
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes


class VideoPlayerFragment : Fragment() {
    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null
    private var volumeState: VolumeState = VolumeState.ON
    private var mediaItem: MediaItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.apply {
            mediaItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelable("media", MediaItem::class.java)
            }else getParcelable("media")
        }

        binding.playerView.clipToOutline = true
        binding.playerView.useController = false
        binding.playerView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 45f)
            }
        }
        exoPlayerSetUp(mediaItem)
        binding.thumbnailImage.loadImage(
            mediaItem?.mediaThumbnail,
            R.drawable.post_placeholder
        )
        binding.playPauseButton.setOnClickListener {
            binding.playPauseButton.remove()
            mediaItem?.let { exoPlayerSetUp(it) }
        }
        binding.speakerImg.setOnClickListener {
            setVolumeControl(volumeState)
        }
    }

    private fun setVolumeControl(state: VolumeState) {
        if (state == VolumeState.OFF) {
            exoPlayer?.volume = 1f
            volumeState = VolumeState.ON
            binding.speakerImg.setImageDrawable(requireActivity().let {
                AppCompatResources.getDrawable(
                    it, R.drawable.speaker_img
                )
            })
        } else if (state == VolumeState.ON) {
            exoPlayer?.volume = 0f
            volumeState = VolumeState.OFF
            binding.speakerImg.setImageDrawable(requireActivity().let {
                AppCompatResources.getDrawable(
                    it, R.drawable.ic_volume_off
                )
            })
        }
    }


    private fun exoPlayerSetUp(model: MediaItem?) {
        exoPlayer = ExoPlayer.Builder(requireActivity()).build()
        val temp = S3Utils.generateS3ShareUrl(requireActivity(), model?.mediaUrl)
        val mediaItem =
            com.google.android.exoplayer2.MediaItem.Builder().setUri(model?.mediaUrl)
                .setMimeType(MimeTypes.APPLICATION_MP4).build()
        val mediaSource =
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireActivity()))
                .createMediaSource(mediaItem)
        binding.playerView.player = exoPlayer

        exoPlayer?.seekTo(0, 0)
        exoPlayer?.setMediaSource(mediaSource, true)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
        exoPlayer?.play()
        //  exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    binding.playPauseButton.remove()
                    binding.thumbnailImage.remove()
                } else if (playbackState == Player.STATE_READY) {
                    binding.playPauseButton.remove()
                    binding.thumbnailImage.remove()
                } else if (playbackState == Player.EVENT_PLAYBACK_STATE_CHANGED) {
                    binding.playPauseButton.show()
                    binding.thumbnailImage.show()
                }
            }
        })
    }

}