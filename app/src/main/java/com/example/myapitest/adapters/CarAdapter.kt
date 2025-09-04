package com.example.myapitest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.databinding.ItemCarBinding
import com.example.myapitest.models.Car

class CarAdapter(
    private val cars: List<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(private val binding: ItemCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            binding.tvCarBrand.text = car.brand
            binding.tvCarModel.text = car.model
            binding.tvCarYear.text = car.year.toString()
            binding.tvCarColor.text = car.color
            binding.tvCarPrice.text = "R$ ${String.format("%.2f", car.price)}"

            // Carregar imagem com Glide - USANDO PLACEHOLDER CORRIGIDO
            Glide.with(binding.root.context)
                .load(car.imageUrl)
                .placeholder(R.drawable.placeholder_car) // ALTERADO
                .error(R.drawable.placeholder_car) // ALTERADO
                .into(binding.ivCarImage)

            binding.root.setOnClickListener {
                onItemClick(car)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(cars[position])
    }

    override fun getItemCount() = cars.size
}
