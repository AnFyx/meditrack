package aiv.vao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "person")
public abstract class Person {

	@Id
	private String email;

	@NotBlank
	private String name;

	private String surname;

	private LocalDate birthDate;

	private LocalDateTime timestamp = LocalDateTime.now();
}
