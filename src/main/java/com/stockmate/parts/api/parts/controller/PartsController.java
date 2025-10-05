package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.entity.Parts;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class PartsController {

//    @GetMapping("/parts")
//    public List<Parts> getParts() {
//
//        ArrayList<Parts> parts = new ArrayList<>();
//        parts.add(new Parts(1L,"자동차바퀴",23));
//        parts.add(new Parts(2L,"자동차 엔진",50));
//        parts.add(new Parts(3L,"자동차 쇼바",40));
//
//        return parts;
//    }
}
