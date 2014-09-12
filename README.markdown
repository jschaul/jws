JWS
===

A scala implementation of
[JSON Web Signatures](https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-31).

This was developed against the `draft-ietf-jose-json-web-signature-31` specification.
Currently only supports a small subset. The HMAC algorithms to be specific. Support for
the rest of the specification will come soon.

Install
-------

Add the following to your `Build.scala`
```
resolvers += "Plasma Conduit Repository" at "http://dl.bintray.com/plamsaconduit/releases",
libraryDependencies += "com.plasmaconduit" %% "jws" % "0.1.0"
```