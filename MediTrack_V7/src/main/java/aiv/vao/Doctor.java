package aiv.vao;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true, exclude = "patients")
@Table(name = "doctor")
public class Doctor extends Person {

    @Getter
    private int maxPatients;

    @JsonbTransient
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Patient> patients = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Doctor other = (Doctor) obj;
        return this.getEmail() != null && this.getEmail().equals(other.getEmail());
    }

    @Override
    public int hashCode() {
        return getEmail() != null ? getEmail().hashCode() : 0;
    }
}
