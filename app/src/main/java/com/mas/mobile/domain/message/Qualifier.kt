package com.mas.mobile.domain.message

sealed class Qualifier(var value: String) {
    abstract fun copy(): Qualifier
}

class CatchQualifier(value: String) : Qualifier(value) {
    override fun copy(): Qualifier = CatchQualifier(value)
}

class SkipQualifier(value: String) : Qualifier(value) {
    override fun copy(): Qualifier = SkipQualifier(value)
}
