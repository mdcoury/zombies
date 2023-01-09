package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.engine.IZombiesGameBroker;
import ca.adaptor.zombies.game.engine.ZombiesGameEngine;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage;
import ca.adaptor.zombies.game.messages.IZombiesWsMessage;
import ca.adaptor.zombies.game.messages.ZombiesGameUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Configuration
@EnableWebSocket
public class ZombiesWsController implements WebSocketConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesWsController.class);
    private static final int TIMEOUT_S = 30;

    private final Map<String, WebSocketSession> theSessionsById = new ConcurrentHashMap<>();
    private final Map<String, IZombiesGameBroker> theBrokersBySessionId = new ConcurrentHashMap<>();
    private final Map<UUID, String> theSessionIdsByBrokerId = new ConcurrentHashMap<>();

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(zombiesWsHandler(), "/zombies/ws");
    }

    @Bean
    public WebSocketHandler zombiesWsHandler() {
        return new ZombiesWsHandler();
    }

    private WebSocketSession getSessionByBrokerId(@NotNull UUID brokerId) {
        return theSessionsById.get(theSessionIdsByBrokerId.get(brokerId));
    }

    @NoArgsConstructor
    public class ZombiesWsHandler implements WebSocketHandler, IZombiesGameBroker.IMessageHandler {
        private final Map<UUID, Consumer<IZombiesWsMessage>> messageCallbacks = new ConcurrentHashMap<>();
        private final UUID handlerId = UUID.randomUUID();
        private final ObjectMapper mapper = new ObjectMapper();

        private <M extends IZombiesWsMessage> void sendMessage(
                @NotNull M outMessage,
                @NotNull UUID brokerId
        ) throws IOException {
            getSessionByBrokerId(
                    brokerId
            ).sendMessage(
                    new TextMessage(
                            mapper.writeValueAsString(outMessage)
                    )
            );
        }

        private void processHelloMessage(@NotNull String sessionId, @NotNull AbstractZombiesWsMessage.Hello hello) {
            assert theSessionsById.containsKey(sessionId);

            var engine = ZombiesGameEngine.getInstance(hello.getGameId());
            var broker = engine.getBroker(hello.getPlayerId());
            if(broker != null) {
                if(!theBrokersBySessionId.containsKey(sessionId)) {
                    LOGGER.trace("Processed HELLO: sessionId=" + sessionId + ", brokerId=" + broker.getBrokerId());
                    theBrokersBySessionId.put(sessionId, broker);
                    theSessionIdsByBrokerId.put(broker.getBrokerId(), sessionId);
                    broker.setMessageHandler(this);
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        private void processRequestReply(@NotNull String sessionId, @NotNull AbstractZombiesWsMessage.AbstractRequestMessage reply) {
            assert theSessionsById.containsKey(sessionId);
            assert theBrokersBySessionId.containsKey(sessionId);
            assert theSessionIdsByBrokerId.containsKey(theBrokersBySessionId.get(sessionId).getBrokerId());
            assert messageCallbacks.containsKey(reply.getMessageId());

            if(theBrokersBySessionId.containsKey(sessionId)) {
                LOGGER.trace("Processing reply: " + reply);
                messageCallbacks.get(reply.getMessageId()).accept(reply);
            }
            else {
                throw new IllegalArgumentException();
            }
        }

//region ZombiesGameBrokerInterface.MessageHandler

        @Override
        @NotNull
        public <M extends IZombiesWsMessage> M send(@NotNull M outMessage, @NotNull UUID brokerId) {
            assert outMessage.getType() != IZombiesWsMessage.Type.HELLO;

            LOGGER.trace("Sending message: " + outMessage);
            var ret = new AtomicReference<M>();
            var messageId = outMessage.getMessageId();
            var latch = new CountDownLatch(1);
            messageCallbacks.put(messageId, (inMessage) -> {
                if(inMessage == null || !inMessage.getMessageId().equals(messageId)) {
                    throw new IllegalArgumentException("Invalid incoming message: " + inMessage);
                }

                LOGGER.trace("Received reply: " + inMessage);
                //noinspection unchecked
                ret.set((M) inMessage);
                latch.countDown();
            });

            try {
                sendMessage(outMessage, brokerId);
                //noinspection ResultOfMethodCallIgnored
                latch.await(TIMEOUT_S, TimeUnit.SECONDS);
                messageCallbacks.remove(messageId);
            }
            catch(IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ret.get();
        }

        @Override
        public void send(@NotNull ZombiesGameUpdateMessage update, @NotNull UUID brokerId) {
            try {
                sendMessage(update, brokerId);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

//endregion

//region WebSocketHandler

        @Override
        public void afterConnectionEstablished(@NotNull WebSocketSession session) {
            if(theSessionsById.containsKey(session.getId())) {
                throw new IllegalArgumentException();
            }

            LOGGER.trace("afterConnectionEstablished: session=" + session + ", handler-id=" + handlerId);
            theSessionsById.put(session.getId(), session);
        }

        @Override
        public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
            if(theSessionsById.containsKey(session.getId())) {
                LOGGER.trace("handleMessage: session=" + session + ", message=" + message + ", handler-id=" + handlerId);
                var wsMessage = mapper.readValue(message.getPayload().toString(), AbstractZombiesWsMessage.class);
                switch (wsMessage.getType()) {
                    case HELLO -> processHelloMessage(session.getId(), (AbstractZombiesWsMessage.Hello) wsMessage);
                    case REQUEST -> processRequestReply(session.getId(), (AbstractZombiesWsMessage.AbstractRequestMessage) wsMessage);
                    default -> throw new IllegalArgumentException();
                }
            }
            else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
            LOGGER.trace("handleTransportError: session=" + session + ", exception=" + exception + ", handler-id=" + handlerId);
        }

        @Override
        public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
            assert theSessionsById.containsKey(session.getId());
            assert theBrokersBySessionId.containsKey(session.getId());

            LOGGER.trace("afterConnectionClosed: session=" + session + ", closeStatus=" + closeStatus + ", handler-id=" + handlerId);
            theSessionsById.remove(session.getId());
            var broker = theBrokersBySessionId.remove(session.getId());

            assert theSessionIdsByBrokerId.containsKey(broker.getBrokerId());
            theSessionIdsByBrokerId.remove(broker.getBrokerId());
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

//endregion

    }
}
