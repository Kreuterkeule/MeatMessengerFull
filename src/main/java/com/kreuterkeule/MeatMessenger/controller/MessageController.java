package com.kreuterkeule.MeatMessenger.controller;

import com.kreuterkeule.MeatMessenger.dto.SendDto;
import com.kreuterkeule.MeatMessenger.models.MessageEntity;
import com.kreuterkeule.MeatMessenger.models.UserEntity;
import com.kreuterkeule.MeatMessenger.repositories.MessageRepository;
import com.kreuterkeule.MeatMessenger.repositories.UserRepository;
import com.kreuterkeule.MeatMessenger.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/message/")
@CrossOrigin
public class MessageController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("getMessages")
    public ResponseEntity<List<MessageEntity>> getMessages(HttpServletRequest request) {
        String jwt = jwtUtils.getJWTFromRequest(request);
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(jwt)).get();
        return ResponseEntity.ok(messageRepository.getAllMessagesByUserToken(client.getIdentifierToken()));
    }

    @GetMapping("getRecentMessages")
    public ResponseEntity<List<MessageEntity>> getRecent(HttpServletRequest request) {
        String jwt = jwtUtils.getJWTFromRequest(request);
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(jwt)).get();
        return new ResponseEntity<>(messageRepository.getAllMessagesByUserToken(client.getIdentifierToken()), HttpStatus.OK);
    }

    @PostMapping("send")
    public ResponseEntity<MessageEntity> sendMessage(@RequestBody SendDto sendDto, HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        UserEntity user = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(token)).get();

        UserEntity receiver = userRepository.findByIdentifierToken(sendDto.getReceiverToken()).get();

        if (receiver == null) {
            return new ResponseEntity<>(new MessageEntity(0, new Timestamp(0), "err", "", "", false), HttpStatus.BAD_REQUEST);
        }

        if (!receiver.getContacts().containsKey(user.getIdentifierToken())) {
            receiver.addToContacts(user.getIdentifierToken(), user.getUsername());
            userRepository.save(receiver);
        }

        MessageEntity toSendMessage = new MessageEntity();

        toSendMessage.setText(sendDto.getMessage());
        toSendMessage.setTo(sendDto.getReceiverToken());
        toSendMessage.setFrom(user.getIdentifierToken());
        toSendMessage.setIsUpdated(false);

        MessageEntity message = messageRepository.save(toSendMessage);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
