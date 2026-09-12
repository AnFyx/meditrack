package aiv.rest;

import aiv.ejb.DoctorDao;
import aiv.vao.Doctor;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/doctors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DoctorRestService {

    @EJB
    private DoctorDao doctorDao;

    @GET
    public List<Doctor> getAllDoctors() {
        return doctorDao.getAll();
    }

    @POST
    public Response addDoctor(Doctor incoming) {
        if (incoming == null || incoming.getEmail() == null || incoming.getEmail().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Doctor email is required").build();
        }

        if (doctorDao.find(incoming.getEmail()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Doctor already exists").build();
        }

        doctorDao.save(incoming);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{email}")
    public Response deleteDoctor(@PathParam("email") String email) {
        Doctor doctor = doctorDao.find(email);
        if (doctor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Doctor not found").build();
        }

        doctorDao.delete(email);
        return Response.noContent().build();
    }

    @GET
    @Path("/{email}")
    public Response getDoctor(@PathParam("email") String email) {
        Doctor d = doctorDao.find(email);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Doctor not found").build();
        }
        return Response.ok(d).build();
    }

}
