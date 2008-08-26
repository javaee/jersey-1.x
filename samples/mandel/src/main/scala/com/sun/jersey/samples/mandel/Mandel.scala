package com.sun.jersey.samples.mandel

object Mandel {
    def iter(c: Complex, limit: int) = {
        def _iter(z: Complex, limit: int): int = {
            if (limit == 0 || z.modSquared >= 4)
                limit
            else
                _iter(z * z + c, limit - 1);
        }

	limit - _iter(c, limit)
    }
}
