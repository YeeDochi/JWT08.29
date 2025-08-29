package com.example.prectice2.Controller;

import com.example.prectice2.DTO.JoinRequestDTO;
import com.example.prectice2.Service.JoinService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;
    
    @PostMapping("/member/join")
    public String M_join(@Valid @RequestBody JoinRequestDTO join) {
        return joinService.join(join, "ROLE_MEMBER");
    }

    @PostMapping("/admin/join")
    public String AD_join(@Valid @RequestBody JoinRequestDTO join) {
        return joinService.join(join, "ROLE_ADMIN");
    }

}
