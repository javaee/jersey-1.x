package mandel

object Mandel {
    def iter(c: Complex, limit: int) = {
	limit - _iter(c, c, limit)
    }

    private def _iter(z: Complex, c: Complex, limit: int) : int = {
	if (limit == 0 || z.modSquared >= 4) return limit

	_iter(z * z + c, c, limit - 1);
    }
}
