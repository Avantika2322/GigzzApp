package com.gigzz.android.ui.connection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAllRequestsBinding
import com.gigzz.android.domain.res.ConnectionData
import com.gigzz.android.presentation.CircleViewModel
import com.gigzz.android.ui.connection.adapter.ConnectionAdapter
import com.gigzz.android.ui.connection.adapter.ConnectionRequestAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllRequestsFragment : Fragment(R.layout.fragment_all_requests) {
    private val binding by viewBinding(FragmentAllRequestsBinding::bind)
    private val viewmodel by activityViewModels<CircleViewModel>()
    private lateinit var connectionAdapter: ConnectionRequestAdapter
    private var myConnectionList = ArrayList<ConnectionData>()
    private var pageNo=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConnectionReq(pageNo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = binding.apply{
        connectionAdapter= ConnectionRequestAdapter { pos, model ,src->
            when(src){
                "accept"->{
                    viewmodel.acceptConnectionRequestApi(model.user_id)
                    myConnectionList.removeAt(pos)
                    connectionAdapter.submitList(myConnectionList)
                }

                "reject"->{
                    viewmodel.rejectConnectionRequestApi(model.user_id)
                    myConnectionList.removeAt(pos)
                    connectionAdapter.submitList(myConnectionList)
                }
            }
        }
        rvConnections.adapter=connectionAdapter

        handleAllConnectionRes()
    }

    private fun handleAllConnectionRes() {
        viewmodel.getAllConnectionRequestRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                rvConnections.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_connection)
                                noDataFound.tvTitle.text=getString(R.string.no_requests)
                                noDataFound.tvSubTitle.text=getString(R.string.no_requests_msg)
                            }else{
                                rvConnections.show()
                                noDataFound.root.hide()
                                if (pageNo == 1) {
                                    myConnectionList.clear()
                                }
                                myConnectionList.addAll(data)
                                connectionAdapter.submitList(myConnectionList)
                            }
                        }
                    }
                }

                is Resource.Error -> binding.progressBar.remove()

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }

}