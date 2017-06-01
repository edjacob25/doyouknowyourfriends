package me.jacobrr.knf

import io.requery.*

/**
 * Created by jacob on 25/05/2017.
 */
@Entity
interface UserAccount : Persistable {
    @get:Key
    @get:Generated
    var id: Int
    var facebookToken: String
    var active: Boolean
}