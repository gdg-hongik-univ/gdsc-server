package com.gdschongik.gdsc.helper;

import com.gdschongik.gdsc.config.TestQuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TestQuerydslConfig.class, DatabaseCleaner.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryTest {

    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @Autowired
    protected TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }
}
