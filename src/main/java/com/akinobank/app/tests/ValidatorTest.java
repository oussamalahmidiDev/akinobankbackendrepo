package com.akinobank.app.tests;

import com.akinobank.app.models.CompteCredentialsRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    public void shouldGiveInvalidNumber () {
        CompteCredentialsRequest c = new CompteCredentialsRequest("2442365958750258", "asdfghjk");
        Set<ConstraintViolation<CompteCredentialsRequest>> violationSet = validator.validate(c);
        assertEquals(0, violationSet.size());
    }

}
