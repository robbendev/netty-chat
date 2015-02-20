package io.ganguo.chat.server.handler;

import io.ganguo.chat.biz.entity.Message;
import io.ganguo.chat.core.connetion.ConnectionManager;
import io.ganguo.chat.core.connetion.IMConnection;
import io.ganguo.chat.core.handler.IMHandler;
import io.ganguo.chat.core.protocol.Commands;
import io.ganguo.chat.core.protocol.Handlers;
import io.ganguo.chat.core.transport.Header;
import io.ganguo.chat.core.transport.IMRequest;
import io.ganguo.chat.core.transport.IMResponse;
import io.ganguo.chat.server.dto.MessageDTO;
import org.springframework.stereotype.Component;

/**
 * Created by Tony on 2/20/15.
 */
@Component
public class MessageHandler extends IMHandler {

    @Override
    public short getId() {
        return Handlers.MESSAGE;
    }

    @Override
    public void dispatch(IMConnection connection, IMRequest request) {
        Header header = request.getHeader();
        switch (header.getCommandId()) {
            case Commands.USER_MESSAGE_REQUEST:
                sendUserMessage(connection, request);
                break;
            default:
                connection.kill();
                break;
        }
    }

    private void sendUserMessage(IMConnection connection, IMRequest request) {
        MessageDTO messageDTO = request.readEntity(MessageDTO.class);
        Message message = messageDTO.getMessage();
        message.setCreateAt(System.currentTimeMillis());

        IMConnection toConn = ConnectionManager.getInstance().get(messageDTO.getTo());
        IMResponse resp = new IMResponse();
        Header header = request.getHeader();
        if (toConn != null) {
            message.setFrom(toConn.getUin());
            resp.setHeader(request.getHeader());
            resp.writeEntity(messageDTO);
            toConn.sendResponse(resp);
        } else {
            header.setCommandId(Commands.ERROR_USER_NOT_EXISTS);
            resp.setHeader(request.getHeader());
            connection.sendResponse(resp);
        }

    }

}