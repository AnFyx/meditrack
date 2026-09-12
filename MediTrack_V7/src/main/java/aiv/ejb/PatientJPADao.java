package aiv.ejb;

import aiv.vao.Patient;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class PatientJPADao implements PatientDao {

    @PersistenceContext(unitName = "hopitalPU")
    private EntityManager em;

    @Override
    public void save(Patient patient) {
        if (find(patient.getEmail()) != null) {
            em.merge(patient);
        } else {
            em.persist(patient);
        }
    }

    @Override
    public void delete(String email) {
        Patient p = find(email);
        if (p != null) {
            em.remove(p);
        }
    }

    @Override
    public Patient find(String email) {
        return em.find(Patient.class, email);
    }

    @Override
    public List<Patient> getAll() {
        return em.createQuery("SELECT p FROM Patient p", Patient.class).getResultList();
    }

    @Override
    public List<Patient> getAssigned() {
        TypedQuery<Patient> query = em.createQuery(
                "SELECT p FROM Patient p WHERE p.doctor IS NOT NULL", Patient.class);
        return query.getResultList();
    }

    @Override
    public List<Patient> getUnassigned() {
        TypedQuery<Patient> query = em.createQuery(
                "SELECT p FROM Patient p WHERE p.doctor IS NULL", Patient.class);
        return query.getResultList();
    }
}
