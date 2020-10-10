package com.fabirt.kpopify.presentation.songplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fabirt.kpopify.databinding.FragmentSongPlayerBinding

class SongPlayerFragment : Fragment() {

    private var _binding: FragmentSongPlayerBinding? = null
    private val binding: FragmentSongPlayerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSongPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}