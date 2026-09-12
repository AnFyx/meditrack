package aiv.jsf;

import aiv.ejb.DoctorDao;
import aiv.vao.Doctor;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Named("DoctorBean")
@ViewScoped
public class DoctorJSFBean implements Serializable {

    @EJB
    private DoctorDao dao;

    @Getter
    @Setter
    private Doctor selectedDoctor = new Doctor();

    @Getter
    private String selectedEmail;

    public List<Doctor> getAllDoctors() throws Exception {
        return dao.getAll();
    }

    public List<Doctor> getAvailableDoctors() throws Exception {
        return dao.getAvailable();
    }

    public String saveDoctor() throws Exception {
        dao.save(selectedDoctor);
        selectedDoctor = new Doctor();
        return "doctors?faces-redirect=true";
    }

    public void deleteDoctor(Doctor doctor) throws Exception {
        dao.delete(doctor.getEmail());
    }

    public void setSelectedEmail(String email) throws Exception {
        selectedEmail = email;
        Doctor d = dao.find(email);
        selectedDoctor = (d != null) ? d : new Doctor();
    }

    public List<Integer> getMaxOptions() {
        return List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    public void init() {
        if (selectedEmail != null && !selectedEmail.equals("NULL")) {
            selectedDoctor = dao.find(selectedEmail);
        } else {
            selectedDoctor = new Doctor();
        }
    }


}
