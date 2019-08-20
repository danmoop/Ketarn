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

    /**
     * This request is handled when user wants to send a message
     * Message will be sent and saved to database
     *
     * @param recipient is the user who gets the message
     * @param message is a message text
     * @param authorUser is the user who sends the message
     *
     * @return dashboard page
     */
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

    /**
     * @see InboxMessage
     *
     * This request is handled when user wants to mark message as done
     * Message will be moved to 'Read' list
     *
     * @param messageID is a message id, taken from a hidden input field
     * @param user is a logged-in user object
     *
     * @return dashboard page
     */
    @PostMapping("/messageIsRead")
    public String messageRead(@RequestParam("messageKey") String messageID, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        InboxMessage msg = userDB.findMessageByMessageKey(messageID);

        if(msg != null)
            userDB.markMessageAsRead(msg);

        userService.save(userDB);

        return "redirect:/dashboard";
    }


    /**
     * @see InboxMessage
     *
     * This request is handled when user wants to delete message forever
     * It will be deleted from 'Read' list
     *
     * @param messageID is a message id, taken from a hidden input field
     * @param user is a logged-in user object
     *
     * @return dashboard page
     */
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

        return "redirect:/dashboard";
    }
}