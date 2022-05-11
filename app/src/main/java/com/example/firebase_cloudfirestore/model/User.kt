package com.example.firebase_cloudfirestore.model

class User {
    var name : String? = null
    var age : Int? = null
    var profileUrl: String? = null

    constructor(name: String?, age: Int?, profileUrl: String?) {
        this.name = name
        this.age = age
        this.profileUrl = profileUrl
    }

    constructor()
}