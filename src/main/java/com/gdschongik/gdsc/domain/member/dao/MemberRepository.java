package com.gdschongik.gdsc.domain.member.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    boolean existsByDiscordUsername(String discordUsername);

    boolean existsByNickname(String nickname);

    boolean existsByUnivEmail(String univEmail);

    Optional<Member> findByDiscordUsername(String discordUsername);

    Optional<Member> findByOauthId(String oauthId);

    Optional<Member> findByStudentId(String studentId);

    List<Member> findAllByName(String name);
}
