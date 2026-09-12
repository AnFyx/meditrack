package aiv.ejb;

import java.util.List;
import aiv.vao.Person;

public interface PersonDao {

	List<Person> getAll();
	Person find(String email);
	void save(Person person);
	void delete(String email);
}