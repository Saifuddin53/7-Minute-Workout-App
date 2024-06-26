package com.myprojects.a7minuteworkoutapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.myprojects.a7minuteworkoutapp.databinding.ItemExerciseStatusBinding

class ExerciseStatusAdapter(val items: ArrayList<ExerciseModel>):
    RecyclerView.Adapter<ExerciseStatusAdapter.ViewHolder>() {

        class ViewHolder(binding: ItemExerciseStatusBinding):
            RecyclerView.ViewHolder(binding.root) {
            val item = binding.tvItem
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemExerciseStatusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: ExerciseModel = items[position]
        holder.item.text = model.getId().toString()

        when {
            model.getIsSelected() -> {
                holder.item.background = ContextCompat.getDrawable(holder.itemView.context,
                    R.drawable.item_circular_color_white_background)
                holder.item.setTextColor(Color.parseColor("#212121"))
            }

            model.getIsCompleted() -> {
                holder.item.background = ContextCompat.getDrawable(holder.itemView.context,
                    R.drawable.cirle_color_accent_background)
                holder.item.setTextColor(Color.parseColor("#FFFFFF"))
            }

            else -> {
                holder.item.background = ContextCompat.getDrawable(holder.itemView.context,
                    R.drawable.item_circular_color_gray_background)
                holder.item.setTextColor(Color.parseColor("#212121"))
            }
        }
    }
}