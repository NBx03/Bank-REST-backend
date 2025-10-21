package com.example.bankcards.controller;

import static com.example.bankcards.testutil.TestDataFactory.createUserDto;
import static com.example.bankcards.testutil.TestDataFactory.createUserRequest;
import static com.example.bankcards.testutil.TestDataFactory.updateUserRolesRequest;
import static com.example.bankcards.testutil.TestDataFactory.updateUserStatusRequest;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UpdateUserRolesRequestDto;
import com.example.bankcards.dto.UpdateUserStatusRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void registerUser_shouldReturnCreatedUser() throws Exception {
        CreateUserRequestDto request = createUserRequest();
        UserDto response = createUserDto(1L);
        when(userService.registerUser(any(CreateUserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    void registerUser_shouldReturnConflictWhenDuplicate() throws Exception {
        when(userService.registerUser(any(CreateUserRequestDto.class)))
                .thenThrow(new DuplicateResourceException("User already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", equalTo("User already exists")));
    }

    @Test
    void getUsers_shouldReturnList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(createUserDto(1L)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUser_shouldReturnSingleUser() throws Exception {
        when(userService.getUser(1L)).thenReturn(createUserDto(1L));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    void getUser_shouldReturnNotFoundWhenMissing() throws Exception {
        when(userService.getUser(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("User not found")));
    }

    @Test
    void updateStatus_shouldReturnUpdatedUser() throws Exception {
        UpdateUserStatusRequestDto request = updateUserStatusRequest(UserStatus.BLOCKED);
        when(userService.updateStatus(eq(1L), eq(UserStatus.BLOCKED))).thenReturn(createUserDto(1L));

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo("john.doe")));
    }

    @Test
    void updateRoles_shouldReturnUpdatedUser() throws Exception {
        UpdateUserRolesRequestDto request = updateUserRolesRequest(Set.of(RoleType.ADMIN));
        UserDto response = new UserDto(1L, "john.doe@example.com", "john.doe", "John", "Doe", UserStatus.ACTIVE, Set.of(RoleType.ADMIN));
        when(userService.assignRoles(eq(1L), eq(Set.of(RoleType.ADMIN)))).thenReturn(response);

        mockMvc.perform(put("/api/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(1)));
    }
}