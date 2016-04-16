package org.jboss.windup.rules.apps.mavenize;

interface ApiDependenciesData
{
    // Regex to help transforming from POM XML:

    // From quickstarts:
    // (?> )(?:<!-- ([\s\S]+?) -->\s+)?<dependency>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+(?:<type>(.+)</type>)?\s+<scope>(.+)</scope>\s+</dependency>
    // MavenCoords DEP_ = new MavenCoords("$2", "$3", "", "$4").setScope("$5").setComment("$1").addExclusion("$");
    // From the BOMs:
    // (?<i> +)(?:<!-- ([\s\S]+?) -->\s+)?<dependency>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+(?:<version>(.+)</version>)?\s+(?:<!-- ([\s\S]+?) -->\s+)?<exclusions>\s+<exclusion>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+</exclusion>\s+</exclusions>\s+(?:<type>(.+)</type>)?\s+(?:<scope>(.+)</scope>)?\s+</dependency>
    // (?<i> +)(?:<!-- ([\s\S]+?) -->\s+)?<dependency>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+(?:<version>(.+)</version>)?\s+(?:<!-- ([\s\S]+?) -->\s+)?(?:<exclusions>\s+<exclusion>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+</exclusion>\s+</exclusions>)?\s+(?:<type>(.+)</type>)?\s+(?:<scope>(.+)</scope>)?\s+</dependency>
    // MavenCoords DEP_ = new MavenCoords("$2", "$3", "$4", "$8").setComment("$1")\n.addExclusion(new MavenCoords("$6", "$7", null).setComment("$5"));
    // (?:<exclusions>\s+)(?:<exclusion>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+</exclusion>)+(?:\s+</exclusions>)
    // (?:<!-- ([\s\S]+?) -->\s+)?<exclusion>\s+<groupId>(.+)</groupId>\s+<artifactId>(.+)</artifactId>\s+</exclusion>
    // .addExclusion(new MavenCoords("$2", "$3", null).setComment("$1"))
    // .setComment\(""\)||.addExclusion\(new MavenCoords\("", "", null\)
    // setScope\(""\) -> setScope\("provided"\)
    // \s+.addExclusion\(new MavenCoords\("", "", null\).setComment\(""\)\); -> ;

    // Quickstarts
    MavenCoords DEP_API_SERVLET_31 = new MavenCoords("org.jboss.spec.javax.servlet", "jboss-servlet-api_3.1_spec", null).setScope("provided");
    MavenCoords DEP_API_EJB_CLIENT = new MavenCoords("org.wildfly", "wildfly-ejb-client-bom", "pom").setScope("provided");
    MavenCoords DEP_API_CDI = new MavenCoords("javax.enterprise", "cdi-api", "", null).setScope("provided").setComment("Import the CDI API, we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_JAVAX_ANN = new MavenCoords("org.jboss.spec.javax.annotation", "jboss-annotations-api_1.2_spec", "", null).setScope("provided").setComment("Import the Common Annotations API (JSR-250), we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_JSF = new MavenCoords("org.jboss.spec.javax.faces", "jboss-jsf-api_2.2_spec", "", null).setScope("provided").setComment("Import the JSF API, we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_EJB_32 = new MavenCoords("org.jboss.spec.javax.ejb", "jboss-ejb-api_3.2_spec", "", null).setScope("provided").setComment("Import the EJB API, we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_JAXRS_20 = new MavenCoords("org.jboss.spec.javax.ws.rs", "jboss-jaxrs-api_2.0_spec", "", null).setScope("provided").setComment("Import the JAX-RS API, we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_JPA_21 = new MavenCoords("org.hibernate.javax.persistence", "hibernate-jpa-2.1-api", "", null).setScope("provided").setComment("Import the JPA API, we use provided scope as the API is included in JBoss EAP");
    MavenCoords DEP_API_HIBERNATE_VALIDATOR = new MavenCoords("org.hibernate", "hibernate-validator", "", null).setScope("provided").addExclusion(new MavenCoords("org.slf4j", "slf4j-api", null));

    // TODO: Maybe I should read these data dynamically from the artifact's POM itself?
    // org.jboss.bom:jboss-eap-javaee7:7.0.0-build-12
    MavenCoords DEP_HBN_CORE = new MavenCoords("org.hibernate", "hibernate-core", "5.0.8.Final", null).setScope("provided").addExclusion(new MavenCoords("xml-apis", "xml-apis", null));
    MavenCoords DEP_HBN_EM = new MavenCoords("org.hibernate", "hibernate-entitymanager", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_HBN_ENVERS = new MavenCoords("org.hibernate", "hibernate-envers", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_HBN_INFINISPAN = new MavenCoords("org.hibernate", "hibernate-infinispan", "5.0.8.Final", null).setScope("provided").addExclusion(new MavenCoords("org.jboss.javaee", "jboss-transaction-api", null));
    MavenCoords DEP_HBN_C3P0 = new MavenCoords("org.hibernate", "hibernate-c3p0", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_HBN_EHCACHE = new MavenCoords("org.hibernate", "hibernate-ehcache", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_HBN_PROXOOL = new MavenCoords("org.hibernate", "hibernate-proxool", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_HBN_VALIDATOR = new MavenCoords("org.hibernate", "hibernate-validator", "5.2.4.Final", null).setScope("provided");
    MavenCoords DEP_HBN_SEARCH_ENGINE = new MavenCoords("org.hibernate", "hibernate-search-engine", "5.5.2.Final", null).setScope("provided");
    MavenCoords DEP_HBN_SEARCH_ORM = new MavenCoords("org.hibernate", "hibernate-search-orm", "5.5.2.Final", null).setScope("provided");
    MavenCoords DEP_ISPAN_DIR_PROVIDER = new MavenCoords("org.infinispan", "infinispan-directory-provider", "8.1.2.Final", null).setScope("provided");
    MavenCoords DEP_HBN_VALID_ANN_PROC = new MavenCoords("org.hibernate", "hibernate-validator-annotation-processor", "5.2.4.Final", null).setScope("provided");
    MavenCoords DEP_HBN_JPA_MD = new MavenCoords("org.hibernate", "hibernate-jpamodelgen", "5.0.8.Final", null).setScope("provided");
    MavenCoords DEP_ISPAN_CORE = new MavenCoords("org.infinispan", "infinispan-core", "8.1.2.Final", null).setScope("provided");
    MavenCoords DEP_ISPAN_CLIENT_HOTROD = new MavenCoords("org.infinispan", "infinispan-client-hotrod", "8.1.2.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_EJB3_EXT = new MavenCoords("org.jboss.ejb3", "jboss-ejb3-ext-api", "2.2.0.Final", null).setScope("provided").addExclusion(new MavenCoords("org.jboss.javaee", "jboss-ejb-api", null));
    MavenCoords DEP_JBOSS_LOGGING_PROC = new MavenCoords("org.jboss.logging", "jboss-logging-processor", "2.0.1.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_LOGGING = new MavenCoords("org.jboss.logging", "jboss-logging", "3.3.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_XTS = new MavenCoords("org.jboss.narayana.xts", "jbossxts", "3.3.0.Final", "5.2.13.Final").setClassifier("api").setScope("provided");
    MavenCoords DEP_RESTEASY_ASSYNC = new MavenCoords("org.jboss.resteasy", "async-http-servlet-3.0", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_ATOM = new MavenCoords("org.jboss.resteasy", "resteasy-atom-provider", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("com.sun.xml.bind", "jaxb-impl", null));
    MavenCoords DEP_RESTEASY_JAXB = new MavenCoords("org.jboss.resteasy", "resteasy-jaxb-provider", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("com.sun.xml.bind", "jaxb-impl", null));
    MavenCoords DEP_RESTEASY_JETTISON = new MavenCoords("org.jboss.resteasy", "resteasy-jettison-provider", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_JACKSON = new MavenCoords("org.jboss.resteasy", "resteasy-jackson-provider", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_JACKSON2 = new MavenCoords("org.jboss.resteasy", "resteasy-jackson2-provider", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_JAXRS = new MavenCoords("org.jboss.resteasy", "resteasy-jaxrs", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("org.jboss.spec.javax.annotation", "jboss-annotations-api_1.1_spec", null)).addExclusion(new MavenCoords("net.jcip", "jcip-annotations", null)).addExclusion(new MavenCoords("org.apache.httpcomponents", "httpclient", null)).addExclusion(new MavenCoords("commons-io", "commons-io", null));
    MavenCoords DEP_RESTEASY_CLIENT = new MavenCoords("org.jboss.resteasy", "resteasy-client", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_MULTIPART = new MavenCoords("org.jboss.resteasy", "resteasy-multipart-provider", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("javax.mail", "mail", null)).addExclusion(new MavenCoords("org.apache.james", "apache-mime4j", null));
    MavenCoords DEP_RESTEASY_JSONP = new MavenCoords("org.jboss.resteasy", "resteasy-json-p-provider", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("javax.json", "javax.json-api", null)).addExclusion(new MavenCoords("org.glassfish", "javax.json", null));
    MavenCoords DEP_RESTEASY_JSAPI = new MavenCoords("org.jboss.resteasy", "resteasy-jsapi", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_RESTEASY_VALIDATOR_11 = new MavenCoords("org.jboss.resteasy", "resteasy-validator-provider-11", "3.0.16.Final", null).setScope("provided").addExclusion(new MavenCoords("org.hibernate", "hibernate-validator", null)).addExclusion(new MavenCoords("com.fasterxml", "classmate", null));
    MavenCoords DEP_RESTEASY_SPRING = new MavenCoords("org.jboss.resteasy", "resteasy-spring", "3.0.16.Final", null).setScope("provided");
    MavenCoords DEP_NEGO_COMMON = new MavenCoords("org.jboss.security", "jboss-negotiation-common", "3.0.0.Final", null).setScope("provided");
    MavenCoords DEP_NEGO_EXTRAS = new MavenCoords("org.jboss.security", "jboss-negotiation-extras", "3.0.0.Final", null).setScope("provided");
    MavenCoords DEP_NEGO_NTLM = new MavenCoords("org.jboss.security", "jboss-negotiation-ntlm", "3.0.0.Final", null).setScope("provided");
    MavenCoords DEP_NEGO_SPNEGO = new MavenCoords("org.jboss.security", "jboss-negotiation-spnego", "3.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSSWS_CXF_CLIENT = new MavenCoords("org.jboss.ws.cxf", "jbossws-cxf-client", "5.1.0.Beta1", null).setScope("provided").addExclusion(new MavenCoords("org.jboss.com.sun.httpserver", "httpserver", null));
    MavenCoords DEP_SUN_HTTPSERVER = new MavenCoords("org.picketbox", "picketbox-commons", "1.0.0.final", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_API = new MavenCoords("org.picketlink", "picketlink-api", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_COMMON = new MavenCoords("org.picketlink", "picketlink-common", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_FEDERATION = new MavenCoords("org.picketlink", "picketlink-federation", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_IMPL = new MavenCoords("org.picketlink", "picketlink-impl", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_IDM_API = new MavenCoords("org.picketlink", "picketlink-idm-api", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_PICKETLINK_IDM_IMPL = new MavenCoords("org.picketlink", "picketlink-idm-impl", "2.5.5.SP1", null).setScope("provided");
    MavenCoords DEP_WFLY_CLUSTERING_SINGLETON = new MavenCoords("org.wildfly", "wildfly-clustering-singleton-api", "10.0.0.Final", null).setScope("provided");
    MavenCoords DEP_WFLY_SECURITY_API = new MavenCoords("org.wildfly", "wildfly-security-api", "10.0.0.Final", null).setScope("provided");
    MavenCoords DEP_LOG4J = new MavenCoords("log4j", "log4j", "1.2.16", null).setScope("provided");
    // From org.jboss.spec:jboss-javaee-7.0:1.0.3.Final
    MavenCoords DEP_JAVAX_ACTIVATION = new MavenCoords("javax.activation", "activation", "1.1.1", null).setScope("provided");
    MavenCoords DEP_JAVAX_CDI_API = new MavenCoords("javax.enterprise", "cdi-api", "1.2", null).setScope("provided").addExclusion(new MavenCoords("javax.annotation", "jsr250-api", null)).addExclusion(new MavenCoords("javax.el", "javax.el-api", null)).addExclusion(new MavenCoords("javax.interceptor", "javax.interceptor-api", null));
    MavenCoords DEP_JAVAX_INJECT = new MavenCoords("javax.inject", "javax.inject", "1", null).setScope("provided");
    MavenCoords DEP_JAVAX_JWS = new MavenCoords("javax.jws", "jsr181-api", "1.0-MR1", null).setScope("provided");
    MavenCoords DEP_JAVAX_MAIL = new MavenCoords("com.sun.mail", "javax.mail", "1.5.3", null).setScope("provided");
    MavenCoords DEP_JAVAX_VALIDATION_API = new MavenCoords("javax.validation", "validation-api", "1.1.0.Final", null).setScope("provided");
    MavenCoords DEP_HBN_JPA_API_21 = new MavenCoords("org.hibernate.javax.persistence", "hibernate-jpa-2.1-api", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAXRS_20 = new MavenCoords("org.jboss.spec.javax.ws.rs", "jboss-jaxrs-api_2.0_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAVAX_ANNOTATION = new MavenCoords("org.jboss.spec.javax.annotation", "jboss-annotations-api_1.2_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAVAX_BATCH = new MavenCoords("org.jboss.spec.javax.batch", "jboss-batch-api_1.0_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_EJB_32 = new MavenCoords("org.jboss.spec.javax.ejb", "jboss-ejb-api_3.2_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAVAX_EL_30 = new MavenCoords("org.jboss.spec.javax.el", "jboss-el-api_3.0_spec", "1.0.4.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_CONCURRENCY_10 = new MavenCoords("org.jboss.spec.javax.enterprise.concurrent", "jboss-concurrency-api_1.0_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JSF_22 = new MavenCoords("org.jboss.spec.javax.faces", "jboss-jsf-api_2.2_spec", "2.2.11", null).setScope("provided");
    MavenCoords DEP_JBOSS_INTERCEPTORS_12 = new MavenCoords("org.jboss.spec.javax.interceptor", "jboss-interceptors-api_1.2_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JSON_10 = new MavenCoords("org.jboss.spec.javax.json", "jboss-json-api_1.0_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_J2EE_MGMT_11 = new MavenCoords("org.jboss.spec.javax.management.j2ee", "jboss-j2eemgmt-api_1.1_spec", "1.0.1.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_CONNECTOR_17 = new MavenCoords("org.jboss.spec.javax.resource", "jboss-connector-api_1.7_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_RMI_10 = new MavenCoords("org.jboss.spec.javax.rmi", "jboss-rmi-api_1.0_spec", "1.0.4.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JACC_15 = new MavenCoords("org.jboss.spec.javax.security.jacc", "jboss-jacc-api_1.5_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JASPI_11 = new MavenCoords("org.jboss.spec.javax.security.auth.message", "jboss-jaspi-api_1.1_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JMS_20 = new MavenCoords("org.jboss.spec.javax.jms", "jboss-jms-api_2.0_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_SERVLET_31 = new MavenCoords("org.jboss.spec.javax.servlet", "jboss-servlet-api_3.1_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JSP_23 = new MavenCoords("org.jboss.spec.javax.servlet.jsp", "jboss-jsp-api_2.3_spec", "1.0.1.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JSTL_12 = new MavenCoords("org.jboss.spec.javax.servlet.jstl", "jboss-jstl-api_1.2_spec", "1.1.2.Final", null).setScope("provided").addExclusion(new MavenCoords("xalan", "xalan", null));
    MavenCoords DEP_JBOSS_TRANSACTION_12 = new MavenCoords("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.2_spec", "1.0.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_WEBSOCKET_11 = new MavenCoords("org.jboss.spec.javax.websocket", "jboss-websocket-api_1.1_spec", "1.1.0.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAXB_22 = new MavenCoords("org.jboss.spec.javax.xml.bind", "jboss-jaxb-api_2.2_spec", "1.0.4.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_SAAJ_13 = new MavenCoords("org.jboss.spec.javax.xml.soap", "jboss-saaj-api_1.3_spec", "1.0.3.Final", null).setScope("provided");
    MavenCoords DEP_JBOSS_JAXWS_22 = new MavenCoords("org.jboss.spec.javax.xml.ws", "jboss-jaxws-api_2.2_spec", "2.0.2.Final", null).setScope("provided");

    MavenCoords[] API_ARTIFACTS = new MavenCoords[]{
        DEP_HBN_CORE,

        DEP_HBN_EM,

        DEP_HBN_ENVERS,
        DEP_HBN_INFINISPAN,
        DEP_HBN_C3P0,
        DEP_HBN_EHCACHE,
        DEP_HBN_PROXOOL,
        DEP_HBN_VALIDATOR,
        DEP_HBN_SEARCH_ENGINE,
        DEP_HBN_SEARCH_ORM,
        DEP_ISPAN_DIR_PROVIDER,
        DEP_HBN_VALID_ANN_PROC,
        DEP_HBN_JPA_MD,
        DEP_ISPAN_CORE,
        DEP_ISPAN_CLIENT_HOTROD,
        DEP_JBOSS_EJB3_EXT,
        DEP_JBOSS_LOGGING_PROC,
        DEP_JBOSS_LOGGING,
        DEP_JBOSS_XTS,
        DEP_RESTEASY_ASSYNC,
        DEP_RESTEASY_ATOM,
        DEP_RESTEASY_JAXB,
        DEP_RESTEASY_JETTISON,
        DEP_RESTEASY_JACKSON,
        DEP_RESTEASY_JACKSON2,
        DEP_RESTEASY_JAXRS,
        DEP_RESTEASY_CLIENT,
        DEP_RESTEASY_MULTIPART,
        DEP_RESTEASY_JSONP,
        DEP_RESTEASY_JSAPI,
        DEP_RESTEASY_VALIDATOR_11,
        DEP_RESTEASY_SPRING,
        DEP_NEGO_COMMON,
        DEP_NEGO_EXTRAS,
        DEP_NEGO_NTLM,
        DEP_NEGO_SPNEGO,
        DEP_JBOSSWS_CXF_CLIENT,
        DEP_SUN_HTTPSERVER,
        DEP_PICKETLINK_API,
        DEP_PICKETLINK_COMMON,
        DEP_PICKETLINK_FEDERATION,
        DEP_PICKETLINK_IMPL,
        DEP_PICKETLINK_IDM_API,
        DEP_PICKETLINK_IDM_IMPL,
        DEP_WFLY_CLUSTERING_SINGLETON,
        DEP_WFLY_SECURITY_API,
        DEP_LOG4J,
        // From org.jboss.spec:jboss-javaee-7.0:1.0.3.Final
        DEP_JAVAX_ACTIVATION,
        DEP_JAVAX_CDI_API,
        DEP_JAVAX_INJECT,
        DEP_JAVAX_JWS,
        DEP_JAVAX_MAIL,
        DEP_JAVAX_VALIDATION_API,
        DEP_HBN_JPA_API_21,
        DEP_JBOSS_JAXRS_20,
        DEP_JBOSS_JAVAX_ANNOTATION,
        DEP_JBOSS_JAVAX_BATCH,
        DEP_JBOSS_EJB_32,
        DEP_JBOSS_JAVAX_EL_30,
        DEP_JBOSS_CONCURRENCY_10,
        DEP_JBOSS_JSF_22,
        DEP_JBOSS_INTERCEPTORS_12,
        DEP_JBOSS_JSON_10,
        DEP_JBOSS_J2EE_MGMT_11,
        DEP_JBOSS_CONNECTOR_17,
        DEP_JBOSS_RMI_10,
        DEP_JBOSS_JACC_15,
        DEP_JBOSS_JASPI_11,
        DEP_JBOSS_JMS_20,
        DEP_JBOSS_SERVLET_31,
        DEP_JBOSS_JSP_23,
        DEP_JBOSS_JSTL_12,
        DEP_JBOSS_TRANSACTION_12,
        DEP_JBOSS_WEBSOCKET_11,
        DEP_JBOSS_JAXB_22,
        DEP_JBOSS_SAAJ_13,
        DEP_JBOSS_JAXWS_22,

    };
}
