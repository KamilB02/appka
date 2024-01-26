package com.example.myapplication



class DataClass  {
    var id: String? = null
    var imageURL: String? = null
    var caption: String? = null
    var warrantyMonths: Int = 0
    var purchaseDate: String? = null
    var returnPeriod: Int = 0
    var store: String? = null
    var price: Double = 0.0

    constructor()

    constructor(id: String?, imageURL: String?, caption: String?, warrantyMonths: Int, purchaseDate: String, returnPeriod: Int, store: String, price: Double) {
        this.id = id
        this.imageURL = imageURL
        this.caption = caption
        this.warrantyMonths = warrantyMonths
        this.purchaseDate = purchaseDate
        this.returnPeriod = returnPeriod
        this.store = store
        this.price = price
    }
}