package com.gigzz.android.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.databinding.ItemOpportunitiesBinding

class OpportunitiesListAdapter(
    private val data: List<String>,
    private val onItemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<OpportunitiesListAdapter.ViewHolder>() {

    private val selectedOption = HashMap<String, Boolean>()

    inner class ViewHolder(private val binding: ItemOpportunitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(opportunity: String) {
            binding.radioButton.apply {
                text = opportunity
                if (selectedOption.containsKey(opportunity)) {
                    this.isChecked = selectedOption[opportunity] == true
                }
                setOnCheckedChangeListener { _, isChecked ->
                    onItemClick(opportunity, isChecked)
                    selectedOption[opportunity] = isChecked
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemOpportunitiesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun clearAllSelections() {
        for (selection in selectedOption) {
            selection.setValue(false)
        }
    }

    fun deselectItem(itemText: String) {
        val position = data.indexOf(itemText)
        if (position != -1) {
            selectedOption[itemText] = false
            notifyItemChanged(position)
        }
    }

}