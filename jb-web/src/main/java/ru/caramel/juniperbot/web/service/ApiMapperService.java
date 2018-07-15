/*
 * This file is part of JuniperBotJ.
 *
 * JuniperBotJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBotJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBotJ. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.caramel.juniperbot.web.service;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import ru.caramel.juniperbot.core.persistence.entity.GuildConfig;
import ru.caramel.juniperbot.module.audio.persistence.entity.MusicConfig;
import ru.caramel.juniperbot.module.custom.persistence.entity.CustomCommand;
import ru.caramel.juniperbot.module.moderation.persistence.entity.ModerationConfig;
import ru.caramel.juniperbot.module.ranking.model.RankingInfo;
import ru.caramel.juniperbot.module.ranking.persistence.entity.RankingConfig;
import ru.caramel.juniperbot.module.welcome.persistence.entity.WelcomeMessage;
import ru.caramel.juniperbot.web.dto.api.RankingInfoDto;
import ru.caramel.juniperbot.web.dto.api.config.*;
import ru.caramel.juniperbot.web.dto.api.discord.GuildShortDto;
import ru.caramel.juniperbot.web.dto.api.discord.RoleDto;
import ru.caramel.juniperbot.web.dto.api.discord.TextChannelDto;
import ru.caramel.juniperbot.web.dto.api.discord.VoiceChannelDto;
import ru.caramel.juniperbot.web.security.model.DiscordGuildDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ApiMapperService {

    @Mappings({
            @Mapping(target = "added", ignore = true),
            @Mapping(target = "members", ignore = true)
    })
    GuildShortDto getGuildDto(DiscordGuildDetails details);

    @Mappings({
            @Mapping(target = "interactable", ignore = true)
    })
    RoleDto getRoleDto(Role role);

    @Mappings({
            @Mapping(source = "NSFW", target = "nsfw"),
            @Mapping(expression = "java(channel.canTalk())", target = "canTalk"),
            @Mapping(target = "permissions", ignore = true)
    })
    TextChannelDto getTextChannelDto(TextChannel channel);

    List<TextChannelDto> getTextChannelDto(List<TextChannel> channels);

    @Mappings({
            @Mapping(target = "permissions", ignore = true)
    })
    VoiceChannelDto getVoiceChannelDto(VoiceChannel channel);

    List<VoiceChannelDto> getVoiceChannelDto(List<VoiceChannel> channels);

    List<GuildShortDto> getGuildDtos(List<DiscordGuildDetails> details);

    @Mappings({
            @Mapping(target = "modConfig", ignore = true)
    })
    CommonConfigDto getCommonDto(GuildConfig config);

    @Mappings({
            @Mapping(target = "prefix", expression = "java(trimmed(source.getPrefix()))"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "guildId", ignore = true),
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "iconUrl", ignore = true),
            @Mapping(target = "disabledCommands", ignore = true)
    })
    void updateCommon(CommonConfigDto source, @MappingTarget GuildConfig target);

    @Mappings({
            @Mapping(expression = "java(ApiMapperService.toStringSet(source.getRoles()))", target = "roles"),
    })
    ModerationConfigDto getModerationDto(ModerationConfig source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "guildConfig", ignore = true),
            @Mapping(expression = "java(ApiMapperService.toLongList(source.getRoles()))", target = "roles"),
    })
    void updateModerationConfig(ModerationConfigDto source, @MappingTarget ModerationConfig target);

    @Mappings({
            @Mapping(expression = "java(ApiMapperService.toString(source.getChannelId()))", target = "channelId"),
            @Mapping(expression = "java(ApiMapperService.toString(source.getTextChannelId()))", target = "textChannelId"),
            @Mapping(expression = "java(ApiMapperService.toStringSet(source.getRoles()))", target = "roles"),
    })
    MusicConfigDto getMusicDto(MusicConfig source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "guildConfig", ignore = true),
            @Mapping(target = "voiceVolume", ignore = true),
            @Mapping(expression = "java(ApiMapperService.toLong(source.getChannelId()))", target = "channelId"),
            @Mapping(expression = "java(ApiMapperService.toLong(source.getTextChannelId()))", target = "textChannelId"),
            @Mapping(expression = "java(ApiMapperService.toLongList(source.getRoles()))", target = "roles"),
    })
    void updateMusicConfig(MusicConfigDto source, @MappingTarget MusicConfig target);

    @Mappings({
            @Mapping(expression = "java(ApiMapperService.toString(source.getAnnouncementChannelId()))", target = "announcementChannelId"),
    })
    RankingDto getRankingDto(RankingConfig source);

    RankingInfoDto getRankingInfoDto(RankingInfo info);

    @Mappings({
            @Mapping(expression = "java(ApiMapperService.toString(source.getJoinChannelId()))", target = "joinChannelId"),
            @Mapping(expression = "java(ApiMapperService.toString(source.getLeaveChannelId()))", target = "leaveChannelId"),
            @Mapping(expression = "java(ApiMapperService.toStringSet(source.getJoinRoles()))", target = "joinRoles"),
    })
    WelcomeDto getWelcomeDto(WelcomeMessage source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "guildConfig", ignore = true),
            @Mapping(expression = "java(ApiMapperService.toLong(source.getJoinChannelId()))", target = "joinChannelId"),
            @Mapping(expression = "java(ApiMapperService.toLong(source.getLeaveChannelId()))", target = "leaveChannelId"),
            @Mapping(expression = "java(ApiMapperService.toLongList(source.getJoinRoles()))", target = "joinRoles"),
    })
    void updateWelcome(WelcomeDto source, @MappingTarget WelcomeMessage target);

    CustomCommandDto getCustomCommandDto(CustomCommand command);

    List<CustomCommandDto> getCustomCommandsDto(List<CustomCommand> command);

    @Mappings({
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "config", ignore = true),
    })
    CustomCommand getCustomCommand(CustomCommandDto command);

    List<CustomCommand> getCustomCommands(List<CustomCommandDto> command);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "config", ignore = true)
    })
    void updateCustomCommand(CustomCommandDto source, @MappingTarget CustomCommand target);

    default String trimmed(String s) {
        return s != null ? s.trim() : null;
    }

    static String toString(Long source) {
        return source != null ? String.valueOf(source) : null;
    }

    static Long toLong(String source) {
        return StringUtils.isNumeric(source) ? Long.valueOf(source) : null;
    }

    static Set<String> toStringSet(Collection<Long> source) {
        return source != null ? source.stream().map(String::valueOf).collect(Collectors.toSet()) : null;
    }

    static List<Long> toLongList(Collection<String> source) {
        return source != null ? source
                .stream()
                .filter(StringUtils::isNumeric)
                .map(Long::valueOf).collect(Collectors.toList()) : null;
    }
}