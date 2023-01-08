package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.engine.ZombiesGameBrokerInterface;
import ca.adaptor.zombies.game.engine.ZombiesGameEngine;
import ca.adaptor.zombies.game.messages.AbstractWsMessage;
import ca.adaptor.zombies.game.messages.WsMessage;
import ca.adaptor.zombies.game.repositories.ZombiesGameRepository;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class ZombiesWsController implements WebSocketConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesWsController.class);

    @Autowired
    private ZombiesGameRepository gameRepository;

    private final Map<String, WebSocketSession> theSessionsByIdMap = new ConcurrentHashMap<>();
    private final Map<String, ZombiesGameBrokerInterface> theBrokersBySessionIdMap = new ConcurrentHashMap<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(zombiesWsHandler(), "/zombies/ws");
    }

    @Bean
    public WebSocketHandler zombiesWsHandler() {
        return new ZombiesWsHandler();
    }

    @NoArgsConstructor
    public class ZombiesWsHandler implements WebSocketHandler {
        private final UUID handlerId = UUID.randomUUID();

        @Override
        public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
            if(theSessionsByIdMap.containsKey(session.getId())) {
                throw new IllegalArgumentException();
            }

            LOGGER.trace("afterConnectionEstablished: session=" + session + ", handler-id=" + handlerId);
            theSessionsByIdMap.put(session.getId(), session);
        }

        @Override
        public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
            if(theSessionsByIdMap.containsKey(session.getId())) {
                LOGGER.trace("handleMessage: session=" + session + ", message=" + message + ", handler-id=" + handlerId);
                var payload = message.getPayload();
                if (WsMessage.class.isAssignableFrom(payload.getClass())) {
                    var wsMessage = (WsMessage) payload;
                    switch (wsMessage.getType()) {
                        case HELLO -> processHelloMessage(session.getId(), (AbstractWsMessage.HelloMessage) wsMessage);
                        case GAME -> processGameMessage(session.getId(), (AbstractWsMessage.GameMessage) wsMessage);
                        default -> throw new IllegalArgumentException();
                    }
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            else {
                throw new IllegalStateException();
            }
        }

        private void processHelloMessage(@NotNull String sessionId, @NotNull AbstractWsMessage.HelloMessage message) {
            assert theSessionsByIdMap.containsKey(sessionId);

            var engine = ZombiesGameEngine.getInstance(message.getGameId());
            var broker = engine.getBroker(message.getPlayerId());
            if(broker != null) {
                if(!theBrokersBySessionIdMap.containsKey(sessionId)) {
                    LOGGER.trace("Processed HELLO: sessionId=" + sessionId + ", brokerId=" + broker.getBrokerId());
                    theBrokersBySessionIdMap.put(sessionId, broker);
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        private void processGameMessage(@NotNull String sessionId, @NotNull AbstractWsMessage.GameMessage message) {
            assert theSessionsByIdMap.containsKey(sessionId);
            assert theBrokersBySessionIdMap.containsKey(sessionId);

            if(theBrokersBySessionIdMap.containsKey(sessionId)) {

            }
            throw new RuntimeException("Not implemented yet");
        }

        @Override
        public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {
            LOGGER.trace("handleTransportError: session=" + session + ", exception=" + exception + ", handler-id=" + handlerId);
        }

        @Override
        public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
            assert theSessionsByIdMap.containsKey(session.getId());
            assert theBrokersBySessionIdMap.containsKey(session.getId());

            LOGGER.trace("afterConnectionClosed: session=" + session + ", closeStatus=" + closeStatus + ", handler-id=" + handlerId);
            theSessionsByIdMap.remove(session.getId());
            theBrokersBySessionIdMap.remove(session.getId());
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

    }
}
