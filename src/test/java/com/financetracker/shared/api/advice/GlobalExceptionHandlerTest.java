package com.financetracker.shared.api.advice;

import com.financetracker.shared.core.exception.DuplicateResourceException;
import com.financetracker.shared.core.exception.ResourceNotFoundException;
import com.financetracker.shared.core.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {
    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MockController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void interceptsResourceNotFoundException_andReturns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Test resource not found"));
    }

    @Test
    void interceptsUnauthorizedAccessException_andReturns403() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Test unauthorized access"));
    }

    @Test
    void interceptsDuplicateResourceException_andReturns409() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Test duplicate resource"));
    }

    @RestController
    private static class MockController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Test resource not found");
        }

        @GetMapping("/test/unauthorized")
        public void throwUnauthorized() {
            throw new UnauthorizedAccessException("Test unauthorized access");
        }

        @GetMapping("/test/duplicate")
        public void throwDuplicate() {
            throw new DuplicateResourceException("Test duplicate resource");
        }
    }
}
