package mandel

object Mandel {
    def iter(c: Complex, limit: int) = {
        var z = c
        var i = 0;
        while(z.modSquared < 4 && i < limit) {
            z = z * z + c;
            i = i + 1;
        }

        i
    }
}