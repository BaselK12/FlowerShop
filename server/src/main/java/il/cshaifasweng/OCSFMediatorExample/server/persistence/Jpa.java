package il.cshaifasweng.OCSFMediatorExample.server.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class Jpa {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("flowershopPU");
    private Jpa() {}
    public static EntityManager em() { return emf.createEntityManager(); }
}
