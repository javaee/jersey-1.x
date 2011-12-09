pkg = {
    "name"          : "jersey-docs-and-examples",
    "version"       : "1.12,0-0.1",
    "attributes"    : { "pkg.summary" : "Jersey Examples And Documentation, RESTful Web services for GlassFish",
                        "pkg.description" : 
"This package contains Jersey API javadocs and examples. Once installed, \
 it's content should appear in <as_home>/glassfish/jersey subdirectory.  \
 Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs." ,
                     "info.classification" : "Web Services"  },
    "dirtrees" : [ "glassfish" ],
    "depends" : { 
                  "pkg:/jersey@1.12,0-0.1" : {"type" : "require" }
                },
    "licenses" : { "glassfish/jersey/LICENSE.txt" : { "license" : "CDDL+GPL" }}
}
