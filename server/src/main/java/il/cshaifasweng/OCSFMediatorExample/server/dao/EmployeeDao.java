package il.cshaifasweng.OCSFMediatorExample.server.dao;

import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;
import jakarta.persistence.*;

import java.util.List;

public class EmployeeDao {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("flowershopPU");

    public Employee save(Employee e) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Employee managed = em.merge(e);
            em.getTransaction().commit();
            return managed;
        } finally {
            em.close();
        }
    }

    public Employee findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try { return em.find(Employee.class, id); }
        finally { em.close(); }
    }

    public List<Employee> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select e from Employee e", Employee.class).getResultList();
        } finally { em.close(); }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Employee e = em.find(Employee.class, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
