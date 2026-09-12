package aiv.jms;

import aiv.dto.DoctorSelectionRequest;
import aiv.ejb.DoctorDao;
import aiv.ejb.PatientDao;
import aiv.vao.Doctor;
import aiv.vao.Patient;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.mail.Session;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/DoctorSelectionQueue"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
        }
)
public class DoctorSelectionConsumer implements MessageListener {

    @Resource(lookup = "java:/mail/MyMail")
    private Session mailSession;

    @Inject
    private PatientDao patientDao;

    @Inject
    private DoctorDao doctorDao;

    @Override
    public void onMessage(Message message) {
        mailSession.setDebug(true);
        try {
            if (message instanceof ObjectMessage objMsg) {
                DoctorSelectionRequest request = (DoctorSelectionRequest) objMsg.getObject();

                Patient patient = patientDao.find(request.getPatientEmail());
                Doctor doctor = doctorDao.find(request.getDoctorEmail());

                if (patient == null || doctor == null) {
                    System.out.println("❌ Patient or doctor not found.");
                    return;
                }

                if (doctor.getPatients().size() >= doctor.getMaxPatients()) {
                    System.out.println("⚠️ Doctor " + doctor.getEmail() + " is at capacity.");
                    sendEmail(patient.getEmail(),
                            "Doctor selection failed",
                            "Sorry, the selected doctor has reached the maximum number of patients.");
                } else {
                    patient.setDoctor(doctor);
                    patientDao.save(patient);
                    System.out.println("✅ Doctor " + doctor.getEmail() + " assigned to patient " + patient.getEmail());

                    sendEmail(patient.getEmail(),
                            "Doctor selection confirmed",
                            "You have been successfully assigned to Dr. " + doctor.getName() + ".");
                    sendEmail(doctor.getEmail(),
                            "New patient assigned",
                            "You have been assigned a new patient: " + patient.getName() + " (" + patient.getEmail() + ")");
                }
            }

        } catch (Exception e) {
            System.out.println("💥 Error while processing doctor selection request.");
        }
    }

    private void sendEmail(String to, String subject, String body) {
        MailSender.send(to, subject, body);
    }
}
