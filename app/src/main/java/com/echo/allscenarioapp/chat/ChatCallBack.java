package com.echo.allscenarioapp.chat;

public interface ChatCallBack {
    // Chat Response  Listener
    public void onChatResponse(SocketIOManager.CHAT_RESPONSE response, Object obj);

}
