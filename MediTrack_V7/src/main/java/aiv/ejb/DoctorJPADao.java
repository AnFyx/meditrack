package aiv.ejb;

import aiv.vao.Doctor;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class DoctorJPADao implements DoctorDao {

    @PersistenceContext(unitName = "hopitalPU")
    private EntityManager em;

    @Override
    public void save(Doctor doctor) {
        if (find(doctor.getEmail()) != null) {
            em.merge(doctor);
        } else {
            em.persist(doctor);
        }
    }

    @Override
    public void delete(String email) {
        Doctor doctor = find(email);
        if (doctor != null) {
            em.remove(doctor);
        }
    }

    @Override
    public Doctor find(String email) {
        return em.find(Doctor.class, email);
    }

    @Override
    public List<Doctor> getAll() {
        return em.createQuery("SELECT d FROM Doctor d", Doctor.class).getResultList();
    }

    @Override
    public List<Doctor> getAvailable() {
        return em.createQuery(
                "SELECT d FROM Doctor d WHERE SIZE(d.patients) < d.maxPatients", Doctor.class
        ).getResultList();
    }

    @Override
    public int countPatients(String doctorEmail) {
        return em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.doctor.email = :email", Long.class)
                .setParameter("email", doctorEmail)
                .getSingleResult()
                .intValue();
    }

}
