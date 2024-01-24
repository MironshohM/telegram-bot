package com.project.SpringDemoBot.service;

import com.project.SpringDemoBot.config.BotConfig;
import com.project.SpringDemoBot.model.User;
import com.project.SpringDemoBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository repository;
    final BotConfig config;
    static final String HELP_TEXT= """
             This bot is created to demonstrate Spring capabilities
             You can execute commands from the main menu on the left or by
             Typing command:
             Type /start to see welcome message.
             Type /mydata to see data stored about you.
             Type /deletedate to delete your data.
             Type /settings to set you preferences.
             Type /help to get help.""";

    public TelegramBot(BotConfig config){
        this.config=config;
        List<BotCommand> listofCommands=new ArrayList<>();
        listofCommands.add(new BotCommand("/start","get a welcome message"));
        listofCommands.add(new BotCommand("/mydata","get your data stored"));
        listofCommands.add(new BotCommand("/deletedata","deleting your data"));
        listofCommands.add(new BotCommand("/help","info how to use this bot"));
        listofCommands.add(new BotCommand("/settings","set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));

        }catch (TelegramApiException e){
           // log.error("Error setting bot's command list "+e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText=update.getMessage().getText();
            long chatId=update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceiver(chatId,update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId,HELP_TEXT);
                    break;

                default:
                    sendMessage(chatId,"Sorry, command was not recognized");
            }
        }

    }



    private void registerUser(Message msg) {
        if(repository.findById(msg.getChatId()).isEmpty()){
            var chatId=msg.getChatId();
            var chat=msg.getChat();
            User user=new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user .setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            repository.save(user);
            //log.info("User saved "+user);

        }

    }

    private void startCommandReceiver(long chatId,String name){
        String answer="Hi, "+name+", nice to meet you!";
        //log.info("Replied to user "+name);
        sendMessage(chatId,answer);

    }
    private void sendMessage(long chatId,String textToSend){
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        }catch (TelegramApiException e){
            //log.error("Error occurred: "+e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
