package com.hfad.android.favdish.utils

object Constants {

    const val DISH_TYPE: String = "DishType"
    const val DISH_CATEGORY: String = "DishCategory"
    const val DISH_COOKING_TIME: String = "DishCookingTime"

    fun dishTypes(): ArrayList<String> {
        val list = ArrayList<String>()

        list.add("breakfast")
        list.add("launch")
        list.add("snacks")
        list.add("dinner")
        list.add("salad")
        list.add("side dish")
        list.add("dessert")
        list.add("other")
        return list
    }

    fun dishCategory(): ArrayList<String> {
        val list = ArrayList<String>()

        list.add("Pizza")
        list.add("BBQ")
        list.add("Bakery")
        list.add("Burger")
        list.add("Chicken")
        list.add("Cafe")
        list.add("Dessert")
        list.add("Drink")
        list.add("Hot Dog")
        list.add("Juices")
        list.add("Sandwich")
        list.add("Wraps")
        list.add("Other")
        return list
    }

    fun dishCookTimes(): ArrayList<String> {
        val list = ArrayList<String>()

        list.add("10")
        list.add("15")
        list.add("20")
        list.add("30")
        list.add("45")
        list.add("60")
        list.add("90")
        list.add("120")
        list.add("150")
        list.add("180")
        return list
    }
}