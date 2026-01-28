package com.example.viom16;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersUser() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"test@example.com\"," +
                                "\"password\":\"strongPass123\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty());
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"invalid-email\"," +
                                "\"password\":\"strongPass123\"" +
                                "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"test2@example.com\"," +
                                "\"password\":\"short\"" +
                                "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsTempDomain() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"user@tempmail.com\"," +
                                "\"password\":\"strongPass123\"" +
                                "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("email domain is not allowed"));
    }

    @Test
    void rejectsPasswordContainingPrefix() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"alice@example.com\"," +
                                "\"password\":\"alice12345\"" +
                                "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("password cannot contain email prefix"));
    }

    @Test
    @DirtiesContext
    void rejectsDuplicateEmail() throws Exception {
        String payload = "{" +
                "\"email\":\"dup@example.com\"," +
                "\"password\":\"strongPass123\"" +
                "}";
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }
}
