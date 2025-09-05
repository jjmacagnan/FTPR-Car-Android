package com.example.myapitest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.databinding.ItemCarLayoutBinding
import com.example.myapitest.models.Car
import com.squareup.picasso.Picasso

class CarAdapter(
    private val onCarClick: (Car) -> Unit
) : ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    inner class CarViewHolder(private val binding: ItemCarLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            binding.apply {
                model.text = car.name
                year.text = car.year
                license.text = car.licence

                Picasso.get()
                    .load(car.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(image)

                root.setOnClickListener { onCarClick(car) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }
}