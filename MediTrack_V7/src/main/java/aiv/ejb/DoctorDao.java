package aiv.ejb;

import aiv.vao.Doctor;

import java.util.List;

public interface DoctorDao {
    void save(Doctor doctor);
    void delete(String email);
    Doctor find(String email);
    List<Doctor> getAll();
    List<Doctor> getAvailable();
    int countPatients(String doctorEmail);
}
