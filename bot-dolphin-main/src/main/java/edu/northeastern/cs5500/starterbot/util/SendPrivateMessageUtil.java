package edu.northeastern.cs5500.starterbot.util;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/** Utility class for sending private messages to users. */
public class SendPrivateMessageUtil {

    /** Private constructor to prevent instantiation of the utility class. */
    private SendPrivateMessageUtil() {}

    /**
     * Sends a message to a user's private channel.
     *
     * @param jda object of jda
     * @param userId discord id of the user
     * @param messageCreateData object of MessageCreateData having embeds
     */
    public static void sendMessage(
            @Nonnull JDA jda,
            @Nonnull String userId,
            @Nonnull MessageCreateData messageCreateData) {
        jda.openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessage(messageCreateData))
                .queue();
    }
}
