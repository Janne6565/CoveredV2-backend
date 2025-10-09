package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.User;
import com.janne.coveredv2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new UserController(userService);
    }

    @Test
    void getAllUsers_returnsUsersFromService() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(userService.getAllUsers()).thenReturn(new User[]{u1, u2});

        ResponseEntity<User[]> response = controller.getAllUsers();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsExactly(u1, u2);
        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void addUser_delegatesToService_andReturnsCreatedUser() {
        User input = mock(User.class);
        User created = mock(User.class);
        when(userService.addUser(input)).thenReturn(created);

        ResponseEntity<User> response = controller.addUser(input);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(created);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).addUser(captor.capture());
        assertThat(captor.getValue()).isSameAs(input);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById_returnsUserFromService() {
        String userId = "abc-123";
        User user = mock(User.class);
        when(userService.getUserById(userId)).thenReturn(user);

        ResponseEntity<User> response = controller.getUserById(userId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(user);
        verify(userService, times(1)).getUserById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUserById_callsService_andReturnsOkMessage() {
        String userId = "to-delete";

        ResponseEntity<String> response = controller.deleteUserById(userId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("User deleted");
        verify(userService, times(1)).deleteUserById(userId);
        verifyNoMoreInteractions(userService);
    }
}
