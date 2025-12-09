package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserService updateUserService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User existing;

    @BeforeEach
    void setUp() {
        existing = new User(
                "u-1",
                "alice@example.com",
                "Alice",
                "hashed-pass",
                "ROLE_USER",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void given_existing_user_when_update_profile_then_persisted_with_new_name_and_email() {

        when(userRepository.findById(eq("u-1"))).thenReturn(Mono.just(existing));
        when(userRepository.existsByEmail(eq("alice2@example.com"))).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(updateUserService.updateProfile("u-1", "Alice Updated", "alice2@example.com"))
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo("u-1");
                    assertThat(updated.name()).isEqualTo("Alice Updated");
                    assertThat(updated.email()).isEqualTo("alice2@example.com");
                })
                .verifyComplete();

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.id()).isEqualTo("u-1");
        assertThat(saved.name()).isEqualTo("Alice Updated");
        assertThat(saved.email()).isEqualTo("alice2@example.com");
    }

    @Test
    void given_missing_user_when_update_profile_then_not_found_error() {

        when(userRepository.findById(eq("u-404"))).thenReturn(Mono.empty());


        StepVerifier.create(updateUserService.updateProfile("u-404", "X", "x@example.com"))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();

        verify(userRepository, never()).save(any());
    }

    @Test
    void given_email_in_use_by_other_when_update_then_conflict() {

        when(userRepository.findById(eq("u-1"))).thenReturn(Mono.just(existing));
        when(userRepository.existsByEmail(eq("taken@example.com"))).thenReturn(Mono.just(true));


        StepVerifier.create(updateUserService.updateProfile("u-1", "Alice", "taken@example.com"))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException)
                .verify();

        verify(userRepository, never()).save(any());
    }

    @Test
    void given_same_email_when_update_then_skip_email_check_and_save() {

        when(userRepository.findById(eq("u-1"))).thenReturn(Mono.just(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));


        StepVerifier.create(updateUserService.updateProfile("u-1", "Alice New", "alice@example.com"))
                .assertNext(updated -> {
                    assertThat(updated.name()).isEqualTo("Alice New");
                    assertThat(updated.email()).isEqualTo("alice@example.com");
                })
                .verifyComplete();

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any());
    }
}