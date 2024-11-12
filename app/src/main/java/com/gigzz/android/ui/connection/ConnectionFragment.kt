package com.gigzz.android.ui.connection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentConnectionBinding
import com.gigzz.android.presentation.CircleViewModel
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadImageFromS3
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConnectionFragment : Fragment(R.layout.fragment_connection) {
    private val binding by viewBinding(FragmentConnectionBinding::bind)
    //private val viewmodel by viewModels<CircleViewModel>()
    private val viewmodel by activityViewModels<CircleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        lifecycleScope.launch {
            binding.topView.userDp.loadImageFromS3(viewmodel.getProfileImg().toString(),R.drawable.user_placeholder)
        }

        binding.topView.userDp.setOnClickListener {
            findNavController().navigate(R.id.action_connectionFragment_to_my_profile_nav_graph)
        }
    }

    private fun initView() = binding.apply {
        topView.ivSearch.hide()
        viewPagerScreen.adapter = JobsViewPagerAdapter(this@ConnectionFragment)
        TabLayoutMediator(
            viewPagerTab, binding.viewPagerScreen
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = "Connections"
                1 -> tab.text = "Requests"
            }
        }.attach()

        viewPagerTab.getTabAt(0)?.setIcon(R.drawable.connection_icon)
        viewPagerTab.getTabAt(1)?.setIcon(R.drawable.ic_request)

        viewPagerTab.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position==0) tab.setIcon(R.drawable.connection_icon)
                else tab.setIcon(R.drawable.ic_request_white)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.position==0) tab.setIcon(R.drawable.ic_connection_blue)
                else tab.setIcon(R.drawable.ic_request)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

    }

    inner class JobsViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UserConnectionsFragment()
                1 -> AllRequestsFragment()
                else -> UserConnectionsFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }

    }
}