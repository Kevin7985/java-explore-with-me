package ru.practicum.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    List<Subscription> findByFromUserId(Long fromUserId);

    List<Subscription> findByToUserId(Long toUserId);
}
