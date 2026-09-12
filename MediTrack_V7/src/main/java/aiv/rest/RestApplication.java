package aiv.rest;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // L’URL de base de tous tes services REST
public class RestApplication extends Application {
}
