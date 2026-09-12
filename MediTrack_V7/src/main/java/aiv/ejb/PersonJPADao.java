package aiv.ejb;

import aiv.vao.Person;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
@Local(PersonDao.class)
public class PersonJPADao implements PersonDao {

	@PersistenceContext(unitName = "hopitalPU")
	private EntityManager em;

	@Override
	public List<Person> getAll() {
		return em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
	}

	@Override
	public Person find(String email) {
		return em.find(Person.class, email);
	}

	@Override
	public void save(Person person) {
		if (find(person.getEmail()) != null) {
			em.merge(person);
		} else {
			em.persist(person);
		}
	}

	@Override
	public void delete(String email) {
		Person person = find(email);
		if (person != null) {
			em.remove(person);
		}
	}
}
