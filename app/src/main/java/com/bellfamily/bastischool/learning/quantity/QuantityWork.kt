package com.bellfamily.bastischool.learning.quantity

/** A bounded editable group, shared by relative quantities and Number Stories.
 * Storage and learning evidence remain with each activity. */
data class QuantityWork(val mask:Int,val undo:List<Int> = emptyList()) {
    fun validate(limit:Int) {
        require(limit in 1..10)
        fun valid(m:Int)=m>=0&&m<(1 shl limit)
        require(valid(mask)&&undo.size<=100&&undo.all(::valid))
        require((undo+mask).zipWithNext().all{(a,b)->Integer.bitCount(a xor b)==1})
    }
    fun toggle(slot:Int,limit:Int):QuantityWork {
        validate(limit)
        return if(slot !in 0 until limit)this else QuantityWork(mask xor(1 shl slot),(undo+mask).takeLast(100))
    }
    fun undo()=if(undo.isEmpty())this else QuantityWork(undo.last(),undo.dropLast(1))
    companion object { fun filled(count:Int):Int {require(count in 0..10);return (1 shl count)-1} }
}
