package aiv.jms;

import aiv.dto.DoctorSelectionRequest;
import aiv.ejb.DoctorDao;
import aiv.ejb.PatientDao;
import aiv.vao.Doctor;
import aiv.vao.Patient;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traite les demandes d'affectation d'un médecin déposées dans la file JMS.
 *
 * <p>Le traitement est asynchrone : la ressource REST accuse réception (202) et
 * c'est ce consommateur qui vérifie le quota, enregistre l'affectation et
 * envoie les notifications.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:/jms/queue/DoctorSelectionQueue"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "jakarta.jms.Queue")
})
public class DoctorSelectionConsumer implements MessageListener {

    private static final Logger LOGGER =
            Logger.getLogger(DoctorSelectionConsumer.class.getName());

    @Inject
    private PatientDao patientDao;

    @Inject
    private DoctorDao doctorDao;

    @Inject
    private MailSender mailSender;

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof ObjectMessage objectMessage)) {
                LOGGER.warning("Unexpected message type on the queue: ignored");
                return;
            }

            Object payload = objectMessage.getObject();
            if (!(payload instanceof DoctorSelectionRequest request)) {
                LOGGER.warning("Unexpected payload type on the queue: ignored");
                return;
            }

            Patient patient = patientDao.find(request.getPatientEmail());
            Doctor doctor = doctorDao.find(request.getDoctorEmail());

            if (patient == null || doctor == null) {
                LOGGER.info("Patient or doctor not found: request dropped");
                return;
            }

            // Le quota est compté en base, comme dans PatientRestService : lire
            // doctor.getPatients().size() dépendrait du chargement de la
            // collection et pouvait donner un résultat différent.
            if (doctorDao.countPatients(doctor.getEmail()) >= doctor.getMaxPatients()) {
                LOGGER.info("Selected doctor is at capacity: request rejected");
                mailSender.send(patient.getEmail(),
                        "Doctor selection failed",
                        "Sorry, the selected doctor has reached the maximum number of patients.");
                return;
            }

            patient.setDoctor(doctor);
            patientDao.save(patient);
            LOGGER.info("Doctor assigned to patient");

            mailSender.send(patient.getEmail(),
                    "Doctor selection confirmed",
                    "You have been successfully assigned to Dr. " + doctor.getName() + ".");
            mailSender.send(doctor.getEmail(),
                    "New patient assigned",
                    "You have been assigned a new patient: " + patient.getName() + ".");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while processing a doctor selection request", e);
        }
    }
}
