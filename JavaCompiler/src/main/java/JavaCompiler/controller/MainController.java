package JavaCompiler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import JavaCompiler.model.JavaModel;
import JavaCompiler.service.MainService;

@RestController
public class MainController {

    @Autowired
    MainService service;

    @PostMapping("/")
    public void JavaCompiler(@RequestBody JavaModel model){
        service.JavaService(model);
    }
}
