package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.server.model.ModelRegistry;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public final class HbBoot {
    private static final SessionFactory SESSION_FACTORY = build();

    private HbBoot() {}

//    private static SessionFactory build() {
//        Configuration cfg = new Configuration();
//
//        // Register all entities in one place
//        for (Class<?> entity : ModelRegistry.entities()) {
//            cfg.addAnnotatedClass(entity);
//        }
//
//        ServiceRegistry registry = new StandardServiceRegistryBuilder()
//                .applySettings(cfg.getProperties())
//                .build();
//
//        return cfg.buildSessionFactory(registry);
//    }
    private static SessionFactory build() {
        // Load hibernate.cfg.xml
        Configuration cfg = new Configuration().configure();

        // Register all entities from your registry
        for (Class<?> entity : ModelRegistry.entities()) {
            cfg.addAnnotatedClass(entity);
        }

        // Explicitly register Complaint (in case it's not in ModelRegistry)
        cfg.addAnnotatedClass(Complaint.class);

        ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties())
                .build();

        return cfg.buildSessionFactory(registry);
    }

    public static SessionFactory sf() {
        return SESSION_FACTORY;
    }
}
