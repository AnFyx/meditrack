package aiv.rest;

import aiv.ejb.DoctorDao;
import aiv.ejb.PatientDao;
import aiv.vao.Patient;
import aiv.vao.Doctor;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientRestService {

    @EJB
    private PatientDao dao;
    @EJB
    private DoctorDao doctorDao;

    @GET
    public List<Patient> getAllPatients() throws Exception {
        return dao.getAll();
    }

    @POST
    public Response addPatient(Patient patient) throws Exception {
        dao.save(patient);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{email}/doctor")
    public Response assignDoctor(@PathParam("email") String patientEmail, Doctor incomingDoctor) throws Exception {
        Patient patient = dao.find(patientEmail);
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }

        Doctor doctor = doctorDao.find(incomingDoctor.getEmail());
        if (doctor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Doctor not found").build();
        }

        int currentCount = doctorDao.countPatients(doctor.getEmail());
        if (currentCount >= doctor.getMaxPatients()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Doctor already has maximum number of patients").build();
        }

        patient.setDoctor(doctor);
        dao.save(patient);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{email}")
    public Response deletePatient(@PathParam("email") String email) throws Exception {
        Patient patient = dao.find(email);
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }

        dao.delete(email);
        return Response.noContent().build(); // 204 No Content
    }

    @GET
    @Path("/{email}")
    public Response getPatient(@PathParam("email") String email) {
        Patient p = dao.find(email);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }
        return Response.ok(p).build();
    }


}
