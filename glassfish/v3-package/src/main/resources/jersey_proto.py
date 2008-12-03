pkg = {
    "name"          : "jersey",
    "version"       : "1.0.1,0-1.0",
    "attributes"    : { "pkg.summary" : "Jersey RESTful Web services for GlassFish",
                        "pkg.description" : 
"Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs.",
                     "info.classification" : "Web Services"  },
    "dirtrees" : [ "jersey", "glassfish"],
    "depends" : { "pkg:/metro@1.4" : {"type" : "require" }},
    "licenses" : { "jersey/LICENSE.txt" : { "license" : "CDDL+GPL" }}
}
