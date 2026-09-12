package aiv.jsf;

import aiv.ejb.PatientDao;
import aiv.ejb.DoctorDao;
import aiv.vao.Patient;
import aiv.vao.Doctor;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named("DetailsBean")
@RequestScoped
public class DetailsJSFBean {

    @EJB
    private PatientDao patientDao;

    @EJB
    private DoctorDao doctorDao;

    @Getter
    @Setter
    private String email;

    @Getter
    private Patient selectedPatient;

    @Getter
    private Doctor selectedDoctor;

    public void init() {
        if (email == null || email.isBlank()) {
            System.out.println("⚠️ Email equals to null");
            return;
        }

        try {
            selectedPatient = patientDao.find(email);
            if (selectedPatient == null) {
                selectedDoctor = doctorDao.find(email);
            }
        } catch (Exception e) {
            System.out.println("❌ Error in DetailsJSFBean.init(): " + e.getMessage());
            e.printStackTrace();
        }
    }

}
