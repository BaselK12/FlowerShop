package il.cshaifasweng.OCSFMediatorExample.server.session;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TX {
    public static void run(Consumer<Session> work) {
        try (Session s = HbBoot.sf().openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                work.accept(s);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    // NEW: same thing but returns a value
    public static <T> T call(Function<Session, T> work) {
        try (Session s = HbBoot.sf().openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                T out = work.apply(s);
                tx.commit();
                return out;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    private TX() {}
}
