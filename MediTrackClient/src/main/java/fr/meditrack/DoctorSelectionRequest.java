package fr.meditrack;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
public class DoctorSelectionRequest implements Serializable {

    @Serial
    @Getter
    private static final long serialVersionUID = 1L;

    @Getter
    private String patientEmail;
    @Getter
    private String doctorEmail;

    public DoctorSelectionRequest() {}

    public DoctorSelectionRequest(String patientEmail, String doctorEmail) {
        this.patientEmail = patientEmail;
        this.doctorEmail = doctorEmail;
    }
}
