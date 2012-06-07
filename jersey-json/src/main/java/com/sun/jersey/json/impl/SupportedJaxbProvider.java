package com.sun.jersey.json.impl;

/**
* @author Michal Gajdos (michal.gajdos at oracle.com)
*/
public enum SupportedJaxbProvider implements JaxbProvider {

    JAXB_RI("com.sun.xml.bind.v2.runtime.JAXBContextImpl", JaxbRiXmlStructure.class),
    MOXY("org.eclipse.persistence.jaxb.JAXBContext", MoxyXmlStructure.class),
    JAXB_JDK("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl", JaxbJdkXmlStructure.class);

    private final String jaxbContextClassName;
    private final Class<? extends DefaultJaxbXmlDocumentStructure> documentStructureClass;

    SupportedJaxbProvider(final String jaxbContextClassName,
                          final Class<? extends DefaultJaxbXmlDocumentStructure> documentStructureClass) {
        this.jaxbContextClassName = jaxbContextClassName;
        this.documentStructureClass = documentStructureClass;
    }

    @Override
    public Class<? extends DefaultJaxbXmlDocumentStructure> getDocumentStructureClass() {
        return documentStructureClass;
    }

    @Override
    public String getJaxbContextClassName() {
        return jaxbContextClassName;
    }

}
