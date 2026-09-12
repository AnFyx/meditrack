package aiv.ejb;

import aiv.vao.Patient;

import java.util.List;

public interface PatientDao {
    void save(Patient patient);
    void delete(String email);
    Patient find(String email);
    List<Patient> getAll();
    List<Patient> getAssigned();
    List<Patient> getUnassigned();
}
