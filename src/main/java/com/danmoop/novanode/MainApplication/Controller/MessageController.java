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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@SessionAttributes(value = "LoggedUser")
public class MessageController
{
    @Autowired
    private UserService userService;

    @PostMapping("/sendInboxMessage")
    public String messageSent(@RequestParam("recipient") String recipient, @RequestParam("messageText") String message, @ModelAttribute("LoggedUser") User authorUser, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userRecipient = userService.findByUserName(recipient);

        if(userRecipient != null)
        {
            InboxMessage inboxMessage = new InboxMessage(message, authorUser.getUserName(), "inboxMessage");

            userRecipient.addMessage(inboxMessage);

            userService.save(userRecipient);

            redirectAttributes.addFlashAttribute("successMsg", "Message sent to " + recipient);
        }

        else
            redirectAttributes.addFlashAttribute("errorMsg", recipient + " is not registered, can't send them a message");

        return "redirect:/dashboard";
    }

    @PostMapping("/messageIsRead")
    public String messageRead(@RequestParam("messageKey") String messageID, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        InboxMessage msg = userDB.findMessageByMessageKey(messageID);

        if(msg != null)
            userDB.markMessageAsRead(msg);

        userService.save(userDB);

        return "redirect:/";
    }

    @PostMapping("/deleteMessage")
    public String messageDeleted(@RequestParam("messageKey") String messageID, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        InboxMessage msg = userDB.findReadMessageByMessageKey(messageID);

        if(msg != null)
        {
            userDB.removeMessage(msg);
            userService.save(userDB);
        }

        return "redirect:/";
    }
}