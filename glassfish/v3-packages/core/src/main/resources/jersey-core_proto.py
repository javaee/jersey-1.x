pkg = {
    "name"          : "jersey",
    "version"       : "1.1.0,0-0.1",
    "attributes"    : { "pkg.summary" : "Jersey Core, RESTful Web services for GlassFish",
                        "pkg.description" : 
"Jersey core runtime libraries including some 3rd party dependencies. \
 Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs.",
                     "info.classification" : "Web Services"  },
    "dirtrees" : [ "glassfish"],
    "depends" : { 
                  "pkg:/glassfish-nucleus@3.0" : {"type" : "require" }
                  ,"pkg:/metro@1.4" : {"type" : "require" }
                },
    "licenses" : { "LICENSE.txt" : { "license" : "CDDL+GPL" }}
}
