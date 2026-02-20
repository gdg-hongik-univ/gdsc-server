package com.gdschongik.gdsc.domain.email.dao;

import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {}
