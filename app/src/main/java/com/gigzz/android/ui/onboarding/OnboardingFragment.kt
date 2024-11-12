package com.gigzz.android.ui.onboarding

import android.os.Bundle
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gigzz.android.R
import com.gigzz.android.databinding.FragmentOnboardingBinding
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.show


class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var pagerAdapter: PagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initListeners()

    }

    private fun initViewPager() {
        pagerAdapter = PagerAdapter(this)
        binding.introPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = true
            offscreenPageLimit = 1
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            requestTransform()
        }
    }

    private fun initListeners() {
        binding.introPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                TransitionManager.beginDelayedTransition(binding.root)
                when (position) {
                    0 -> {
                        binding.indicatorImg.setImageResource(R.drawable.ind1)
                        binding.skip.show()
                        binding.startBtn.hide()
                    }

                    1 -> {
                        binding.indicatorImg.setImageResource(R.drawable.ind2)
                        binding.skip.show()
                        binding.startBtn.hide()
                    }

                    2 -> {
                        binding.indicatorImg.setImageResource(R.drawable.ind3)
                        binding.skip.hide()
                        binding.startBtn.show()
                    }
                }

            }
        })
        binding.skip.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signInFragment)
        }
        binding.startBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signInFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> Onboarding1Fragment()
            1 -> Onboarding2Fragment()
            2 -> Onboarding3Fragment()
            else -> Onboarding1Fragment()
        }
    }

}