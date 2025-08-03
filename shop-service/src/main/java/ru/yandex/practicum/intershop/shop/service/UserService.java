package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String login) {
        return userRepository.findByLogin(login)
                .doOnNext(user -> log.info("Найден пользователь: {}", user.getLogin()))
                .map(user -> User.withUsername(user.getLogin())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build())
                .doOnSuccess(u -> log.debug("UserDetails успешно создан для {}", login))
                .doOnError(e -> log.error("Ошибка при загрузке пользователя {}", login, e));
    }
}
