package com.example.spring_auth_app.spring_auth_app.utilities;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUUID(String id){
        return UUID.fromString(id);
    }
}
