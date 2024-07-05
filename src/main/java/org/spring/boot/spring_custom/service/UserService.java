package org.spring.boot.spring_custom.service;

import org.spring.boot.spring_custom.spring.annotations.Service;

@Service
public class UserService {


    public String add(String name) {
        return name;
    }

    public String find(String name) {
        return name + "ok";
    }
}
