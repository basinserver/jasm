package com.protryon.jasm

class Constant<T>(val value: T) {

    override fun toString(): String {
        if (value is JType) {
            return value.toString()
        } else if (value is Method) {
            return (value as Method).descriptor.niceString((value as Method).name)
        } else if (value is Field) {
            return (value as Field).name + " " + (value as Field).type.niceName
        } else if (value is Local) {
            return "v" + (value as Local).index
        }
        return value.toString()
    }

}
