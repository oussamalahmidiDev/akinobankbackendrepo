package com.akinobank.app.controllers.admin;


import com.akinobank.app.models.User;
import com.akinobank.app.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@ResponseBody //The @ResponseBody annotation tells a controller that the object returned is automatically serialized into JSON , you will need it
@RequestMapping("/admin")
public class AdminPanelController {

    final String ADMIN_VIEWS_PATH = "views/admin/";


    @GetMapping("")
    public String index(Model model) {
        return ADMIN_VIEWS_PATH + "index";
    }

    @GetMapping("users")
    public String usersView(Model model) {
        return ADMIN_VIEWS_PATH + "users";
    }

    @GetMapping("users/ajouter")
    public String addUserView(Model model) {
        /// juste pour tester le form.
        User test = User.builder().email("test").password("hello").build();
        model.addAttribute("user", test);
        return ADMIN_VIEWS_PATH + "forms/adduser";
    }
    @PostMapping("users/ajouter")
    public String greetingSubmit(@ModelAttribute User user) {
        /// juste pour tester le form.
        return ADMIN_VIEWS_PATH + "result";
    }




}
