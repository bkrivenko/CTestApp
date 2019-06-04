package com.hetum.testapp.ui

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hetum.testapp.R
import com.hetum.testapp.databinding.ItemResultBinding
import com.hetum.testapp.model.Result


class ResultAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    private var results: List<Result> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding: ItemResultBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_result, parent, false)
        return ResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(results[position])
        holder.ctv.setOnClickListener {
            toggleChecked(position, listener)
        }
    }

    fun updateData(results: List<Result>) {
        this.results = results
        notifyDataSetChanged()
    }

    private fun toggleChecked(position: Int, listener: OnItemClickListener) {
        results[position].isChecked = !results[position].isChecked
        listener.onItemClick(results[position])
        notifyDataSetChanged()
    }

    class ResultViewHolder(private val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root) {

        val ctv = binding.ctv

        fun bind(result: Result) {
            binding.result = result
            binding.executePendingBindings()
        }
    }
}