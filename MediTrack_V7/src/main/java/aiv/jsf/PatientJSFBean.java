package aiv.jsf;

import java.io.Serializable;
import java.util.List;

import aiv.ejb.PatientDao;
import aiv.jms.MailSender;
import aiv.vao.Patient;
import aiv.vao.Doctor;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.ejb.EJB;

import lombok.Getter;
import lombok.Setter;

@Named("PatientBean")
@ViewScoped
public class PatientJSFBean implements Serializable {

    @EJB
    private PatientDao dao;

    @Inject
    private MailSender mailSender;

    @Getter
    @Setter
    private Patient selectedPatient = new Patient();

    @Getter
    @Setter
    private String selectedEmail;

    public void init() {
        if (selectedEmail != null && !selectedEmail.equals("NULL")) {
            System.out.println("📥 Chargement du patient : " + selectedEmail);
            selectedPatient = dao.find(selectedEmail);
        } else {
            selectedPatient = new Patient();
        }
    }

    public List<Patient> Assigned() throws Exception {
        return dao.getAssigned();
    }

    public List<Patient> Unassigned() throws Exception {
        return dao.getUnassigned();
    }

    public List<Patient> AllPeople() throws Exception {
        return dao.getAll();
    }

    public String savePatient() throws Exception {
        System.out.println(">>> SAVE triggered <<<");
        System.out.println("Patient : " + selectedPatient);

        dao.save(selectedPatient);

        // Envoi d'email après sauvegarde
        String to = selectedPatient.getEmail();
        String subject = "Assignment information";
        String content;

        Doctor doctor = selectedPatient.getDoctor();
        if (doctor == null) {
            content = "Hello, you are not, or no longer, assigned to a doctor.";
        } else {
            content = "Hello, you have been assigned to Dr : " + doctor.getName();
        }

        try {
            mailSender.send(to, subject, content);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to);
            e.printStackTrace();
        }

        selectedPatient = new Patient();
        return "patients?faces-redirect=true";
    }

    public void deletePatient(Patient patient) throws Exception {
        dao.delete(patient.getEmail());
    }
}
