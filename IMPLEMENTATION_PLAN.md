# Implementation Plan - Message Center & Chat & Private Trading Refactor

## 1. Objective
Refactor the platform to support private trading via real-time chat, removing complex order flows, and adding a centralized message center.

## 2. Backend Implementation (WebSocket)

### 2.1 Dependencies
- Add `spring-boot-starter-websocket` to `pom.xml`.

### 2.2 Database
- Create `bus_chat_message` table:
  - `id` (Long)
  - `sender_id` (Long)
  - `receiver_id` (Long)
  - `content` (Text)
  - `type` (Int: 0-Text, 1-Image, 2-System)
  - `goods_id` (Long, Optional - for context)
  - `is_read` (Int: 0-Unread, 1-Read)
  - `create_time` (DateTime)

### 2.3 Java Classes
- **Config**: `WebSocketConfig.java`
- **Server**: `WebSocketServer.java` (Endpoint `/ws/{userId}`)
  - Handle `onOpen`, `onClose`, `onMessage`, `onError`.
  - Maintain `ConcurrentHashMap<String, Session>` for active connections.
- **Entity**: `ChatMessage.java`
- **Controller**: `ChatController.java` (API for history: `/chat/history`, `/chat/sessions`)
- **Service**: `ChatService.java`

## 3. Frontend Implementation

### 3.1 New Pages
- `pages/message/index.vue` (Message Center)
  - Lists active chat sessions + System notifications.
- `pages/message/chat.vue` (Chat Room)
  - WebSocket handling (`uni.connectSocket`).
  - Chat UI (Bubbles).
  - "I Want" context (Show goods snippet at top).

### 3.2 Page Updates
- **Home (`pages/index/index.vue`)**:
  - Add Bell Icon to custom navbar.
  - Link to `pages/message/index`.
- **Goods Detail (`pages/goods-detail/index.vue`)**:
  - "I Want" button: Navigate to `pages/message/chat?targetUserId=...&goodsId=...`.
- **Publish Goods (`pages/publish/goods/index.vue`)**:
  - Remove "Trading Method" picker.
- **Mine (`pages/mine/index.vue`)**:
  - Remove "My Sold", "My Bought".
  - Add entry to "My Messages" or rely on Home bell.
  - Keep "My Published", "My Favorites".

## 4. Execution Steps
1.  **Backend**: Add dependency & create SQL.
2.  **Backend**: Implement WebSocket & Chat APIs.
3.  **Frontend**: Create Message Pages (List & Chat).
4.  **Frontend**: Connect Home -> Message Center.
5.  **Frontend**: Connect Goods Detail -> Chat.
6.  **Frontend**: Refactor Publish & Mine pages.
