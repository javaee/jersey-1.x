pkg = {
    "name"          : "jersey-docs-and-examples",
    "version"       : "1.0.3,0-0.1",
    "attributes"    : { "pkg.summary" : "Jersey Examples And Documentation, RESTful Web services for GlassFish",
                        "pkg.description" : 
"Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs. \
This package contains Jersey apidocs and examples",
                     "info.classification" : "Web Services"  },
    "dirtrees" : [ "glassfish" ],
    "depends" : { 
                  "pkg:/jersey@1.0.3" : {"type" : "require" }
                },
    "licenses" : { "glassfish/jersey/LICENSE.txt" : { "license" : "CDDL+GPL" }}
}
