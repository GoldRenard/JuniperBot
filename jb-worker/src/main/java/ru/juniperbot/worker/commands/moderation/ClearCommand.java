/*
 * This file is part of JuniperBot.
 *
 * JuniperBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBot. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.juniperbot.worker.commands.moderation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import ru.juniperbot.common.model.exception.DiscordException;
import ru.juniperbot.common.model.exception.ValidationException;
import ru.juniperbot.common.utils.CommonUtils;
import ru.juniperbot.common.worker.command.model.BotContext;
import ru.juniperbot.common.worker.command.model.DiscordCommand;
import ru.juniperbot.common.worker.command.model.MemberReference;
import ru.juniperbot.common.worker.modules.audit.service.ActionsHolderService;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@DiscordCommand(key = "discord.command.mod.clear.key",
        description = "discord.command.mod.clear.desc",
        group = "discord.command.group.moderation",
        permissions = {
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS,
                Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_HISTORY
        }
)
public class ClearCommand extends MentionableModeratorCommand {

    private static final int MAX_MESSAGES = 1000;

    private static final Pattern COUNT_PATTERN = Pattern.compile("^(\\d+)");

    @Autowired
    private ActionsHolderService actionsHolderService;

    public ClearCommand() {
        super(true, false);
    }

    @Override
    protected boolean doCommand(MemberReference reference, GuildMessageReceivedEvent event, BotContext context, String query) throws DiscordException {
        Member member = reference.getMember();

        boolean includeInvocation = reference.isAuthorSelected() || (member != null && member.equals(event.getMember()));
        int number = getCount(query) + (includeInvocation ? 1 : 0);

        String userId = reference.isAuthorSelected() ? null : reference.getId();

        DateTime limit = new DateTime()
                .minusWeeks(2)
                .minusHours(1);

        TextChannel channel = event.getChannel();
        channel.sendTyping().queue(r -> channel.getIterableHistory()
                .takeAsync(number)
                .thenApplyAsync(e -> {
                    Stream<Message> stream = e.stream()
                            .filter(m -> CommonUtils.getDate(m.getTimeCreated()).isAfter(limit));
                    if (StringUtils.isNotEmpty(userId)) {
                        stream = stream.filter(m -> m.getMember() != null
                                && Objects.equals(m.getMember().getUser().getId(), userId));
                    }
                    List<Message> messageList = stream.collect(Collectors.toList());
                    messageList.forEach(actionsHolderService::markAsDeleted);
                    channel.purgeMessages(messageList);
                    return messageList.size();
                }).exceptionally(e -> {
                    log.error("Clear failed", e);
                    fail(event);
                    return null;
                }).whenCompleteAsync((count, e) -> contextService.withContext(context.getConfig().getGuildId(), () -> {
                    int finalCount = count;
                    if (includeInvocation) {
                        finalCount--;
                    }
                    if (finalCount <= 0) {
                        messageService.onEmbedMessage(event.getChannel(), "discord.mod.clear.absent");
                        return;
                    }
                    String pluralMessages = messageService.getCountPlural(finalCount, "discord.plurals.message");
                    messageService.onTempMessage(channel, 5, "discord.mod.clear.deleted", finalCount, pluralMessages);
                })));
        return true;
    }

    private int getCount(String queue) throws DiscordException {
        int result = 10;
        if (StringUtils.isNotBlank(queue)) {
            Matcher matcher = COUNT_PATTERN.matcher(queue.trim());
            if (!matcher.find()) {
                throw new ValidationException("discord.mode.clear.help");
            }
            try {
                result = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                result = MAX_MESSAGES;
            }
        }
        return Math.min(result, MAX_MESSAGES);
    }
}
