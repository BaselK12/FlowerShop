package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class Smoke {

    public static void main(String[] args) {
        long aliceId = createDemoData();      // add two rows and return Alice's id
        printAll("After insert");

        deleteById(aliceId);                  // simulate your delete handler
        printAll("After delete");

        // optional: try deleting a non-existing ID to see the "no-op" behavior
        deleteById(999999);
    }

    private static long createDemoData() {
        final long[] idHolder = {0};
        TX.run((Session s) -> {
            Employee alice = new Employee("Alice","Female","alice@ex.com","111-222","Manager",true,5000, LocalDate.now());
            Employee bob   = new Employee("Bob","Male","bob@ex.com","333-444","Cashier",true,3000, LocalDate.now());
            s.save(alice);
            s.save(bob);
            s.flush();                 // forces INSERTs so IDs are generated now
            idHolder[0] = alice.getId();
        });
        return idHolder[0];
    }

    private static void deleteById(long id) {
        TX.run((Session s) -> {
            Employee e = s.get(Employee.class, id);
            if (e != null) {
                s.remove(e);
                System.out.printf("[SMOKE] Deleted id=%d%n", id);
            } else {
                System.out.printf("[SMOKE] Not found id=%d (nothing to delete)%n", id);
            }
        });
    }

    private static void printAll(String label) {
        TX.run((Session s) -> {
            List<Employee> list = s.createQuery("from Employee", Employee.class).getResultList();
            System.out.println("---- " + label + " ----");
            list.forEach(e -> System.out.printf(
                    "%d | %s | %s | %s | %s | %b | %d | %s%n",
                    e.getId(), e.getName(), e.getGender(), e.getEmail(), e.getRole(),
                    e.isActive(), e.getSalary(), e.getHireDate()
            ));
            if (list.isEmpty()) System.out.println("(no rows)");
        });
    }
}
