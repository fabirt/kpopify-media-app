package com.fabirt.kpopify.presentation.musicplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fabirt.kpopify.databinding.FragmentMusicPlayerBinding

class MusicPlayerFragment : Fragment() {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding: FragmentMusicPlayerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}