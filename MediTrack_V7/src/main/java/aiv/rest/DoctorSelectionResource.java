package aiv.rest;

import aiv.dto.DoctorSelectionRequest;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.Resource;

@Path("/doctor-selection")
public class DoctorSelectionResource {

    @Inject
    JMSContext context;

    @Resource(lookup = "java:/jms/queue/DoctorSelectionQueue")
    private Queue queue;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendToQueue(DoctorSelectionRequest request) {
        try {
            context.createProducer().send(queue, request);
            return Response.accepted().build(); // HTTP 202
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build(); // HTTP 500
        }
    }
}
