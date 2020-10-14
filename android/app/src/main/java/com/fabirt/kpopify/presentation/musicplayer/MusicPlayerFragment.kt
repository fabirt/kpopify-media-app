package com.fabirt.kpopify.presentation.musicplayer

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.fabirt.kpopify.R
import com.fabirt.kpopify.core.exoplayer.isPlaying
import com.fabirt.kpopify.core.exoplayer.toSong
import com.fabirt.kpopify.core.util.bindNetworkImage
import com.fabirt.kpopify.databinding.FragmentMusicPlayerBinding
import com.fabirt.kpopify.presentation.viewmodels.MusicPlayerViewModel
import com.google.android.material.transition.MaterialContainerTransform

class MusicPlayerFragment : Fragment() {

    companion object {
        private const val TAG = "MusicPlayerFragment"
    }

    private val playerViewModel: MusicPlayerViewModel by activityViewModels()

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding: FragmentMusicPlayerBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTransitions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickActions()
        subscribeToObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setOnClickActions() {
        binding.includedPlayerControls.fabPlayPause.setOnClickListener {
            playerViewModel.playOrPauseCurrentSong()
        }

        binding.includedPlayerControls.ivPrevious.setOnClickListener {
            playerViewModel.skipToPreviousSong()
        }

        binding.includedPlayerControls.ivNext.setOnClickListener {
            playerViewModel.skipToNextSong()
        }

        binding.includedPlayerControls.ivReplay.setOnClickListener {
            playerViewModel.toggleRepeatMode()
        }
    }

    private fun subscribeToObservers() {
        playerViewModel.currentPlayingSong.observe(viewLifecycleOwner, Observer { mediaItem ->
            mediaItem?.description?.mediaId?.let {
                val song = mediaItem.toSong()
                binding.tvTitle.text = song.title
                binding.tvArtist.text = song.artist
                bindNetworkImage(binding.ivSong, song.imageUrl)
            }
        })

        playerViewModel.playbackState.observe(viewLifecycleOwner, Observer { playbackState ->
            val iconResource =
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

            binding.includedPlayerControls.fabPlayPause.setImageResource(iconResource)

            val replayModeColor = if (playerViewModel.isInRepeatMode)
                requireContext().getColor(R.color.colorOnSurface)
            else Color.WHITE

            binding.includedPlayerControls.ivReplay.setColorFilter(replayModeColor)
        })
    }

    private fun configureTransitions() {
        val color = requireContext().getColor(R.color.colorPrimary)
        val transition = MaterialContainerTransform().apply {
            duration = 300L
            containerColor = color
            drawingViewId = R.id.nav_host_fragment
        }
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }
}
