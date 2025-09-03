package il.cshaifasweng.OCSFMediatorExample.server.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

public final class Tx {
    private Tx() {}
    public static <T> T inTx(Function<EntityManager, T> work) {
        EntityManager em = Jpa.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
