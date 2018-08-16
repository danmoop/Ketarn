package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.InboxMessage;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@SessionAttributes(value = "LoggedUser")
public class MessageController
{
    @Autowired
    UserService userService;

    @PostMapping("/sendInboxMessage")
    public String messageSent(@RequestParam("recipient") String recepient, @RequestParam("messageText") String message, @ModelAttribute("LoggedUser") User authorUser) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userRecepient = userService.findByUserName(recepient);

        if(userRecepient != null)
        {
            InboxMessage inboxMessage = new InboxMessage(message, authorUser.getUserName(), "inboxMessage");

            userRecepient.addMessage(inboxMessage);

            userService.save(userRecepient);

            return "redirect:/";
        }

        return "redirect:/";
    }

    @PostMapping("/messageIsRead")
    public String messageRead(@RequestParam("messageKey") String messageID, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.markMessageAsRead(userDB.findMessageByMessageKey(messageID));

        userService.save(userDB);

        return "redirect:/";
    }

    @PostMapping("/deleteMessage")
    public String messageDeleted(@RequestParam("messageKey") String messageID, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.removeMessage(userDB.findReadMessageByMessageKey(messageID));

        userService.save(userDB);

        return "redirect:/";
    }
}