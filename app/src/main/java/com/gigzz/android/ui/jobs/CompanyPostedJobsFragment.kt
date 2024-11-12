package com.gigzz.android.ui.jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCompanyPostedJobsBinding
import com.gigzz.android.databinding.FragmentJobsBinding

class CompanyPostedJobsFragment : Fragment(R.layout.fragment_company_posted_jobs) {
    private val binding by viewBinding(FragmentCompanyPostedJobsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}