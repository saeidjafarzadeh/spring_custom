package org.spring.boot.spring_custom.conroller;

import org.spring.boot.spring_custom.service.UserService;
import org.spring.boot.spring_custom.spring.annotations.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private static UserService userService;

    @PostMapping("/api")
    public String add(@RequestBody String body) {
        return userService.add(body);
    }

}
