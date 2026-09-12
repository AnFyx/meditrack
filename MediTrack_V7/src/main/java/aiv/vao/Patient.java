package aiv.vao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@Table(name = "patient")
public class Patient extends Person {

    private String details;

    @ManyToOne
    @JoinColumn(name = "doctor_email")
    private Doctor doctor;
}
