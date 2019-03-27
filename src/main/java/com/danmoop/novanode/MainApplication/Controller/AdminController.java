package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class AdminController
{
    @Autowired
    private UserService userService;

    @PostMapping("/BanUser")
    public String banUser(@RequestParam("ban_userName") String userName)
    {
        User userDB = userService.findByUserName(userName);

        if(userDB != null)
        {
            userDB.setBanned(true);
            userService.save(userDB);
        }

        return "redirect:/admin";
    }

    @PostMapping("/UnbanUser")
    public String unbanUser(@RequestParam("unban_userName") String userName)
    {
        User userDB = userService.findByUserName(userName);

        if(userDB != null)
        {
            userDB.setBanned(false);
            userService.save(userDB);
        }

        return "redirect:/admin";
    }

    @PostMapping("/givesub")
    public String giveSub(@RequestParam("sub_userName") String userName)
    {
        User userDB = userService.findByUserName(userName);

        userDB.setHasBoughtSubs(true);

        userService.save(userDB);

        return "redirect:/admin";
    }

    @PostMapping("/giveuuid")
    public String giveuuid(RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addFlashAttribute("uuid", "UUID: " + UUID.randomUUID());
        return "redirect:/admin";
    }
}