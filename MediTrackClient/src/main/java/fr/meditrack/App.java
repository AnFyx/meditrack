package fr.meditrack;

import com.google.gson.Gson;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;


public class App {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;

        do {
            System.out.println("\n===== MENU =====");
            System.out.println("1. 📋 List all patients");
            System.out.println("2. ➕ Add a new patient");
            System.out.println("3. ➕ Add a new doctor");
            System.out.println("4. 🗑️ Delete a patient");
            System.out.println("5. 👨‍⚕️ List all doctors");
            System.out.println("6. 🔄 Assign a doctor to a patient");
            System.out.println("7. 📩 Request a doctor (via JMS)");
            System.out.println("0. ❌ Exit");
            System.out.print("👉 Your choice: ");

            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> listAllPatients();
                case 2 -> sendNewPatient(promptNewPatient());
                case 3 -> sendNewDoctor(promptNewDoctor());
                case 4 -> {
                    System.out.print("✉️ Email of the patient to delete: ");
                    String email = scanner.nextLine();
                    deletePatient(email);
                }
                case 5 -> listAllDoctors();
                case 6 -> assignDoctorToPatient();
                case 7 -> sendDoctorSelectionRequestPrompt();
                case 0 -> System.out.println("👋 Goodbye!");
                default -> System.out.println("❌ Invalid choice!");
            }

        } while (choice != 0);
    }

    // ---------- PROMPTS ----------
    private static Patient promptNewPatient() {
        Patient p = new Patient();
        System.out.print("👤 First name: ");
        p.name = scanner.nextLine();
        System.out.print("👤 Last name: ");
        p.surname = scanner.nextLine();
        System.out.print("✉️ Email: ");
        p.email = scanner.nextLine();
        System.out.print("🎂 Birth date (YYYY-MM-DD): ");
        p.birthDate = scanner.nextLine();
        System.out.print("📝 Details: ");
        p.details = scanner.nextLine();
        return p;
    }

    private static Doctor promptNewDoctor() {
        Doctor d = new Doctor();
        System.out.print("👤 First name: ");
        d.name = scanner.nextLine();
        System.out.print("👤 Last name: ");
        d.surname = scanner.nextLine();
        System.out.print("✉️ Email: ");
        d.email = scanner.nextLine();
        System.out.print("👥 Max number of patients: ");
        d.maxPatients = Integer.parseInt(scanner.nextLine());
        return d;
    }

    // ---------- HTTP REST METHODS ----------
    private static HttpURLConnection createConnection(String endpoint, String method) throws Exception {
        URL url = new URL("http://localhost:8080/MediTrack/api/" + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        if (method.equals("POST") || method.equals("PUT")) {
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
        } else {
            con.setRequestProperty("Accept", "application/json");
        }
        return con;
    }

    public static void listAllPatients() {
        try {
            HttpURLConnection con = createConnection("patients", "GET");
            int code = con.getResponseCode();

            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }
                in.close();

                Gson gson = new Gson();
                Patient[] patients = gson.fromJson(json.toString(), Patient[].class);

                System.out.println("📋 Patient list:");
                for (Patient p : patients) {
                    System.out.println("———————————————");
                    System.out.println("👤 Name: " + p.name + " " + p.surname);
                    System.out.println("✉️ Email: " + p.email);
                    System.out.println("🎂 Birth date: " + p.birthDate);
                    System.out.println("📝 Details: " + p.details);
                    if (p.doctor != null) {
                        System.out.println("👨‍⚕️ Doctor: " + p.doctor.name + " (" + p.doctor.email + ")");
                    } else {
                        System.out.println("👨‍⚕️ Doctor: None assigned");
                    }
                }

            } else {
                System.out.println("❌ HTTP error: " + code);
            }

            con.disconnect();
        } catch (Exception e) {
            System.out.println("💥 Error while fetching patients: " + e.getMessage());
        }
    }

    public static void listAllDoctors() {
        try {
            HttpURLConnection con = createConnection("doctors", "GET");
            int code = con.getResponseCode();

            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }
                in.close();

                Gson gson = new Gson();
                Doctor[] doctors = gson.fromJson(json.toString(), Doctor[].class);

                System.out.println("🩺 Doctor list:");
                for (Doctor d : doctors) {
                    System.out.println("———————————————");
                    System.out.println("👤 Name: " + d.name + " " + d.surname);
                    System.out.println("✉️ Email: " + d.email);
                    System.out.println("👥 Max patients: " + d.maxPatients);
                }

            } else {
                System.out.println("❌ HTTP error: " + code);
            }

            con.disconnect();
        } catch (Exception e) {
            System.out.println("💥 Error while fetching doctors: " + e.getMessage());
        }
    }

    public static void sendNewPatient(Patient patient) {
        try {
            HttpURLConnection con = createConnection("patients", "POST");
            String json = new Gson().toJson(patient);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int code = con.getResponseCode();
            switch (code) {
                case 200, 201 -> System.out.println("✅ Patient added successfully!");
                case 400 -> System.out.println("❗ Invalid data provided. Please check the fields.");
                case 409 -> System.out.println("❗ A patient with this email already exists.");
                default -> System.out.println("❌ Unexpected error (" + code + ") while adding patient.");
            }

            con.disconnect();
        } catch (Exception e) {
            System.out.println("💥 Error while adding patient: " + e.getMessage());
        }
    }

    public static void sendNewDoctor(Doctor doctor) {
        try {
            HttpURLConnection con = createConnection("doctors", "POST");
            String json = new Gson().toJson(doctor);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int code = con.getResponseCode();
            if (code == 200 || code == 201) {
                System.out.println("✅ Doctor added successfully!");
            } else if (code == 409) {
                System.out.println("❗ A doctor with this email already exists.");
            } else {
                System.out.println("❌ Unexpected error (" + code + ") while adding doctor.");
            }

            con.disconnect();
        } catch (Exception e) {
            System.out.println("💥 Error while adding doctor: " + e.getMessage());
        }
    }

    public static void deletePatient(String email) {
        try {
            HttpURLConnection con = createConnection("patients/" + email, "DELETE");
            int code = con.getResponseCode();

            if (code == 204) {
                System.out.println("🗑️ Patient deleted successfully!");
            } else if (code == 404) {
                System.out.println("❗ Patient not found: " + email);
            } else {
                System.out.println("❌ Unexpected error (" + code + ") while deleting patient.");
            }

            con.disconnect();
        } catch (Exception e) {
            System.out.println("💥 Error while deleting patient: " + e.getMessage());
        }
    }

    public static Patient[] getAllPatients() {
        try {
            HttpURLConnection con = createConnection("patients", "GET");
            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) json.append(line);
                in.close();
                con.disconnect();
                return new Gson().fromJson(json.toString(), Patient[].class);
            }
            con.disconnect();
        } catch (Exception ignored) {}
        return null;
    }

    public static Patient getPatientByEmail(String encodedEmail) {
        try {
            HttpURLConnection con = createConnection("patients/" + encodedEmail, "GET");
            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json = in.readLine();
                in.close();
                con.disconnect();
                return new Gson().fromJson(json, Patient.class);
            }
            con.disconnect();
        } catch (Exception ignored) {}
        return null;
    }

    public static Doctor getDoctorByEmail(String encodedEmail) {
        try {
            HttpURLConnection con = createConnection("doctors/" + encodedEmail, "GET");
            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json = in.readLine();
                in.close();
                con.disconnect();
                return new Gson().fromJson(json, Doctor.class);
            }
            con.disconnect();
        } catch (Exception ignored) {}
        return null;
    }

    public static void assignDoctorToPatient() {
        try {
            System.out.print("Enter patient email: ");
            String patientEmail = scanner.nextLine().trim();

            System.out.print("Enter doctor email: ");
            String doctorEmail = scanner.nextLine().trim();

            String encodedPatientEmail = URLEncoder.encode(patientEmail, StandardCharsets.UTF_8);
            String encodedDoctorEmail = URLEncoder.encode(doctorEmail, StandardCharsets.UTF_8);

            Patient patient = getPatientByEmail(encodedPatientEmail);
            if (patient == null) {
                System.out.println("❌ Patient not found.");
                return;
            }

            Doctor doctor = getDoctorByEmail(encodedDoctorEmail);
            if (doctor == null) {
                System.out.println("❌ Doctor not found.");
                return;
            }

            Patient[] allPatients = getAllPatients();
            long assignedCount = Arrays.stream(allPatients)
                    .filter(p -> p.doctor != null && doctorEmail.equalsIgnoreCase(p.doctor.email))
                    .count();

            if (assignedCount >= doctor.maxPatients) {
                System.out.printf("❌ Doctor is full (%d/%d patients).%n", assignedCount, doctor.maxPatients);
                return;
            }

            String json = new Gson().toJson(doctor);
            URL url = new URL("http://localhost:8080/MediTrack/api/patients/" + encodedPatientEmail + "/doctor");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int code = con.getResponseCode();
            if (code == 200) {
                System.out.println("✅ Doctor successfully assigned to patient.");
            } else {
                System.out.println("❌ Error assigning doctor (HTTP " + code + ")");
            }

            con.disconnect();

        } catch (Exception e) {
            System.out.println("💥 Exception: " + e.getMessage());
        }
    }

    public static void sendDoctorSelectionRequestPrompt() {
        System.out.print("Enter patient email: ");
        String patientEmail = scanner.nextLine().trim();

        System.out.print("Enter doctor email: ");
        String doctorEmail = scanner.nextLine().trim();

        try {
            HttpURLConnection con = createConnection("doctor-selection", "POST");

            DoctorSelectionRequest request = new DoctorSelectionRequest(patientEmail, doctorEmail);
            String json = new Gson().toJson(request);

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = con.getResponseCode();
            if (code == 202) {
                System.out.println("✅ Selection request sent.");
            } else {
                System.out.println("❌ Server responded with HTTP " + code);
            }

            con.disconnect();

        } catch (Exception e) {
            System.out.println("💥 Error while sending request: " + e.getMessage());
        }
    }


}
