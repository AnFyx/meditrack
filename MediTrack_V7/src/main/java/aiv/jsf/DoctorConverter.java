package aiv.jsf;

import aiv.ejb.DoctorDao;
import aiv.vao.Doctor;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(value = "doctorConverter", managed = true)
public class DoctorConverter implements Converter<Doctor> {

    @EJB
    private DoctorDao doctorDao;

    @Override
    public Doctor getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) return null;
        return doctorDao.find(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Doctor doctor) {
        if (doctor == null) return "";
        return doctor.getEmail();
    }
}
