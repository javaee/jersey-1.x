
Issues:

- Maybe make add injection to ctors to enforce certain behavior. For example,
  a RefreshableView base class could be used to enforce that instances have
  ETag and cache TTL information.

- How to implement *built-in* support for conditional requests? (see above)

-