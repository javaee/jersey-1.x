package foo;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessProducer;

public class TestExtensionBean implements Extension {

    public static List<ProcessInjectionTarget> l = new ArrayList<ProcessInjectionTarget>();

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        System.out.println("WEB BEFORE BEAN DISCOVERY: " + event.getClass());
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        System.out.println("WEB AFTER BEAN DISCOVERY: " + manager.getClass());
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
        System.out.println("WEB AFTER DEPLOYMENT VALIDATION");
    }

    void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat) {
        System.out.println("WEB PROCESS ANNOTATED TYPE: " + pat.getAnnotatedType());
    }

    void processInjectionTarget(@Observes ProcessInjectionTarget<?> pit) {
        System.out.println("WEB PROCESS INJECTION TARGET: " + pit);
        System.out.println("  AnnotatedType: " + pit.getAnnotatedType());

        InjectionTarget<?> it = pit.getInjectionTarget();
        for (InjectionPoint ip : it.getInjectionPoints()) {
            System.out.println("  InjectionPoint: " + ip);
        }
        l.add(pit);
    }

    void processProducer(@Observes ProcessProducer<?, ?> pp) {
        System.out.println("WEB PROCESS PRODUCER: " + pp);
    }

    void processBean(@Observes ProcessManagedBean<?> pb) {
        System.out.println("WEB PROCESS MANAGED BEAN: " + pb);
    }
}
