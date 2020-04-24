package com.akinobank.app.services;

import com.akinobank.app.models.User;
import lombok.Data;
import org.springframework.stereotype.Service;

// this class is just for the tests.

@Service
@Data
public class AuthService {

    private User currentUser;


}
