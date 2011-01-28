pkg = {
    "name"          : "jersey",
    "version"       : "1.6,0-0.3",
    "attributes"    : { "pkg.summary" : "Jersey Core, RESTful Web services for GlassFish",
                        "pkg.description" : 
"Jersey core runtime libraries including some 3rd party dependencies. \
 Documentation and examples of Jersey were moved to a separate package, Jersey Examples And Documentation. \
 If you are about to upgrade Jersey, you might want to install that new package as well, otherwise Jersey examples and javadocs will get deleted. \
 Also please note, that from 1.0.3 version on, Jersey docs and examples get installed into <as_home>/glassfish/jersey directory instead of just <as_home>/jersey. \
 Jersey is the open source (under dual CDDL+GPL license)\
 JAX-RS (JSR 311) Reference Implementation for building RESTful Web services. \
 But, it is also more than the Reference Implementation. \
 Jersey provides additional APIs and extension points (SPIs) \
 so that developers may extend Jersey to suite their needs.",
                     "info.classification" : "Web Services"  },
    "dirtrees" : [ "glassfish"],
    "depends" : { 
                  "pkg:/glassfish-nucleus@3.0" : {"type" : "require" }
                  ,"pkg:/glassfish-common" : {"type" : "require" }
                  ,"pkg:/metro@1.4" : {"type" : "require" }
                },
    "licenses" : { "LICENSE.txt" : { "license" : "CDDL+GPL" }, "third-party-license-readme.txt" : { "license" : "Apache2" }}
}
