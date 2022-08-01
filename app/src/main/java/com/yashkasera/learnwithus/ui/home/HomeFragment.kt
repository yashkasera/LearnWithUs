package com.yashkasera.learnwithus.ui.home

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yashkasera.learnwithus.databinding.FragmentHomeBinding
import java.util.*


class HomeFragment : Fragment(){
    private val binding by lazy {
        FragmentHomeBinding.inflate(LayoutInflater.from(context))
    }
    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.story.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNarrationFragment())
        }
        binding.grammar.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToGrammarFragment())
        }
    }
}