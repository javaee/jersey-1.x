pkg = {
    "name"          : "jersey-all",
    "version"       : "1.0.3,0-0.1",
    "attributes"    : { "pkg.summary" : "Jersey RESTful Web services for GlassFish, API Documentation and Examples",
                        "pkg.description" : 
"Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs. \
This package covers all available Jersey packages.",
                     "info.classification" : "Web Services"  },
    "depends" : { 
                  "pkg:/jersey-docs-and-examples@1.0.3" : {"type" : "require" }
                },
    "licenses" : { "LICENSE.txt" : { "license" : "CDDL+GPL" }}
}
