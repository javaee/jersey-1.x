package com.sun.jersey.samples.mandel

object Complex {
    val i = new Complex(0, 1)
    implicit def double2complex(x: double): Complex = new Complex(x, 0)
    implicit def double2complex(x: int): Complex = new Complex(x.toDouble, 0)
}

class Complex(val re: double, val im: double) {

    def + (that: Complex): Complex = new Complex(this.re + that.re, this.im + that.im)
    def - (that: Complex): Complex = new Complex(this.re - that.re, this.im - that.im)
    def * (that: Complex): Complex = new Complex(this.re * that.re - this.im * that.im, 
                                                 this.re * that.im + this.im * that.re) 
    def / (that: Complex): Complex = { 
        val denom = that.modSquared 
        new Complex((this.re * that.re + this.im * that.im) / denom, 
                    (this.im * that.re - this.re * that.im) / denom) 
    }
 
    def modSquared = re * re + im * im

    def mod = Math.sqrt(modSquared)

    override def toString = re+( if (im < 0) "-"+(- im) else "+"+ im)+"* I"
}