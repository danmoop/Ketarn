package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.InboxMessage;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class MessageController {

    @Autowired
    private UserRepository userRepository;

    /**
     * This request is handled when user wants to send a message
     * Message will be sent and saved to database
     *
     * @param message    is a message text
     * @param recipient  is the user who gets the message
     * @param authAuthor is the user who sends the message
     * @return dashboard page
     */
    @PostMapping("/sendInboxMessage")
    public String messageSent(@RequestParam String recipient, @RequestParam("messageText") String message, Principal authAuthor, RedirectAttributes redirectAttributes) {
        if (authAuthor == null) {
            return "redirect:/";
        }

        User userRecipient = userRepository.findByUserName(recipient);
        User authorUser = userRepository.findByUserName(authAuthor.getName());

        if (authorUser.isBanned()) {
            return userIsBanned();
        }

        if (userRecipient != null) {
            InboxMessage inboxMessage = new InboxMessage(message, authorUser.getUserName(), "inboxMessage");

            userRecipient.getMessages().add(inboxMessage);
            userRepository.save(userRecipient);

            redirectAttributes.addFlashAttribute("successMsg", "Message sent to " + recipient);
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", recipient + " is not registered, can't send them a message");
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to mark message as done
     * Message will be moved to 'Read' list
     *
     * @param messageKey is a message id, taken from a hidden input field
     * @param auth       is a logged-in user object
     * @return dashboard page
     */
    @PostMapping("/messageIsRead")
    public String messageRead(@RequestParam String messageKey, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        InboxMessage msg = user.findMessageByMessageKey(messageKey);

        if (msg != null) {
            user.markMessageAsRead(msg);
            userRepository.save(user);
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to delete message forever
     * It will be deleted from 'Read' list
     *
     * @param messageKey is a message id, taken from a hidden input field
     * @param auth       is a logged-in user object
     * @return dashboard page
     */
    @PostMapping("/deleteMessage")
    public String messageDeleted(@RequestParam String messageKey, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        InboxMessage msg = user.findReadMessageByMessageKey(messageKey);

        if (msg != null) {
            user.getMessages().remove(msg);
            userRepository.save(user);
        }

        return "redirect:/dashboard";
    }

    private String userIsBanned() {
        SecurityContextHolder.clearContext();
        return "handlingPages/youarebanned";
    }
}