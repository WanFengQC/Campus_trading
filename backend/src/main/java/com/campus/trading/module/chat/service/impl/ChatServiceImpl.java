package com.campus.trading.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.chat.dto.ChatContactResponse;
import com.campus.trading.module.chat.dto.ChatMessageResponse;
import com.campus.trading.module.chat.entity.ChatMessageEntity;
import com.campus.trading.module.chat.entity.ChatSessionEntity;
import com.campus.trading.module.chat.mapper.ChatMessageMapper;
import com.campus.trading.module.chat.mapper.ChatSessionMapper;
import com.campus.trading.module.chat.service.ChatService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    public ChatServiceImpl(ChatSessionMapper chatSessionMapper,
                           ChatMessageMapper chatMessageMapper,
                           UserMapper userMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ChatContactResponse> listContacts(Long userId) {
        List<ChatSessionEntity> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSessionEntity>()
            .and(w -> w.eq(ChatSessionEntity::getUserAId, userId).or().eq(ChatSessionEntity::getUserBId, userId))
            .orderByDesc(ChatSessionEntity::getLastMessageAt)
            .orderByDesc(ChatSessionEntity::getCreatedAt));
        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> peerIds = new LinkedHashSet<>();
        for (ChatSessionEntity session : sessions) {
            peerIds.add(getPeerUserId(userId, session));
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(peerIds)) {
            userMap.put(user.getId(), user);
        }

        List<ChatContactResponse> result = new ArrayList<>();
        for (ChatSessionEntity session : sessions) {
            Long peerUserId = getPeerUserId(userId, session);
            UserEntity peer = userMap.get(peerUserId);
            Long unreadCount = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, session.getId())
                .eq(ChatMessageEntity::getToUserId, userId)
                .eq(ChatMessageEntity::getReadStatus, 0));

            result.add(ChatContactResponse.builder()
                .sessionId(session.getId())
                .peerUserId(peerUserId)
                .peerName(resolveDisplayName(peer))
                .lastMessage(session.getLastMessage())
                .lastMessageAt(session.getLastMessageAt())
                .unreadCount(unreadCount == null ? 0L : unreadCount)
                .build());
        }
        return result;
    }

    @Override
    public List<ChatMessageResponse> listConversation(Long userId, Long peerUserId, Integer limit) {
        if (peerUserId == null || peerUserId <= 0) {
            return new ArrayList<>();
        }
        ChatSessionEntity session = findSession(userId, peerUserId);
        if (session == null) {
            return new ArrayList<>();
        }

        int safeLimit = (limit == null || limit <= 0) ? 50 : Math.min(limit, 200);
        List<ChatMessageEntity> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
            .eq(ChatMessageEntity::getSessionId, session.getId())
            .orderByDesc(ChatMessageEntity::getCreatedAt)
            .last("limit " + safeLimit));
        if (messages.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.reverse(messages);

        Set<Long> userIds = new LinkedHashSet<>();
        for (ChatMessageEntity message : messages) {
            userIds.add(message.getFromUserId());
            userIds.add(message.getToUserId());
        }
        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        List<ChatMessageResponse> result = new ArrayList<>();
        for (ChatMessageEntity message : messages) {
            UserEntity fromUser = userMap.get(message.getFromUserId());
            result.add(ChatMessageResponse.builder()
                .messageId(message.getId())
                .fromUserId(message.getFromUserId())
                .fromUserName(resolveDisplayName(fromUser))
                .toUserId(message.getToUserId())
                .content(message.getContent())
                .mine(userId.equals(message.getFromUserId()))
                .readStatus(message.getReadStatus())
                .createdAt(message.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long fromUserId, Long toUserId, String content) {
        if (toUserId == null || toUserId <= 0) {
            throw new BusinessException("接收方用户不存在");
        }
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能给自己发送消息");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息内容不能为空");
        }
        UserEntity toUser = userMapper.selectById(toUserId);
        if (toUser == null || toUser.getStatus() == null || toUser.getStatus() != 1) {
            throw new BusinessException("接收方用户不存在或已禁用");
        }

        ChatSessionEntity session = findOrCreateSession(fromUserId, toUserId);
        String normalized = limit(content.trim(), 500);

        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(session.getId());
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(normalized);
        message.setReadStatus(0);
        chatMessageMapper.insert(message);

        session.setLastMessage(limit(normalized, 255));
        session.setLastMessageAt(LocalDateTime.now());
        chatSessionMapper.updateById(session);
        return message.getId();
    }

    @Override
    public void markConversationRead(Long userId, Long peerUserId) {
        ChatSessionEntity session = findSession(userId, peerUserId);
        if (session == null) {
            return;
        }
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessageEntity>()
            .set(ChatMessageEntity::getReadStatus, 1)
            .eq(ChatMessageEntity::getSessionId, session.getId())
            .eq(ChatMessageEntity::getToUserId, userId)
            .eq(ChatMessageEntity::getReadStatus, 0));
    }

    private ChatSessionEntity findOrCreateSession(Long userId1, Long userId2) {
        Pair pair = normalizePair(userId1, userId2);
        ChatSessionEntity session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSessionEntity>()
            .eq(ChatSessionEntity::getUserAId, pair.userAId)
            .eq(ChatSessionEntity::getUserBId, pair.userBId)
            .last("limit 1"));
        if (session != null) {
            return session;
        }

        ChatSessionEntity created = new ChatSessionEntity();
        created.setUserAId(pair.userAId);
        created.setUserBId(pair.userBId);
        created.setLastMessage("");
        created.setLastMessageAt(LocalDateTime.now());
        chatSessionMapper.insert(created);
        return created;
    }

    private ChatSessionEntity findSession(Long userId1, Long userId2) {
        Pair pair = normalizePair(userId1, userId2);
        return chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSessionEntity>()
            .eq(ChatSessionEntity::getUserAId, pair.userAId)
            .eq(ChatSessionEntity::getUserBId, pair.userBId)
            .last("limit 1"));
    }

    private Long getPeerUserId(Long currentUserId, ChatSessionEntity session) {
        return currentUserId.equals(session.getUserAId()) ? session.getUserBId() : session.getUserAId();
    }

    private Pair normalizePair(Long userId1, Long userId2) {
        if (userId1 <= userId2) {
            return new Pair(userId1, userId2);
        }
        return new Pair(userId2, userId1);
    }

    private String resolveDisplayName(UserEntity user) {
        if (user == null) {
            return "未知用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "未知用户";
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record Pair(Long userAId, Long userBId) {
    }
}
