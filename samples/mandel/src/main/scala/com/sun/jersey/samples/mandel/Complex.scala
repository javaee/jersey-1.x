package com.sun.jersey.samples.mandel

object Complex {
    def apply(re: Double, im: Double) : Complex = new Complex(re, im);

    val i = Complex(0, 1)

    implicit def double2complex(x: Double): Complex = Complex(x, 0)

    implicit def double2complex(x: Int): Complex = Complex(x.toDouble, 0)
}

class Complex(val re: Double, val im: Double) {

    def + (that: Complex): Complex = Complex(this.re + that.re, this.im + that.im)

    def - (that: Complex): Complex = Complex(this.re - that.re, this.im - that.im)

    def * (that: Complex): Complex = Complex(this.re * that.re - this.im * that.im, 
                                             this.re * that.im + this.im * that.re) 
    def / (that: Complex): Complex = { 
        val denom = that.modSquared 
        Complex((this.re * that.re + this.im * that.im) / denom, 
                (this.im * that.re - this.re * that.im) / denom) 
    }
 
    def modSquared = re * re + im * im

    def mod = Math.sqrt(modSquared)

    override def toString = re+( if (im < 0) "-"+(- im) else "+"+ im)+"* I"
}